package dev.voidcrates

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.voidcrates.commands.BaseCommand
import dev.voidcrates.commands.KeysCommand
import dev.voidcrates.config.ConfigManager
import dev.voidcrates.config.GenericClickType
import dev.voidcrates.config.Lang
import dev.voidcrates.config.SoundOption
import dev.voidcrates.data.actions.Action
import dev.voidcrates.data.opening.world.WorldOpeningAnimation
import dev.voidcrates.data.particles.effects.ParticleEffect
import dev.voidcrates.data.rewards.Reward
import dev.voidcrates.data.rewards.options.bool.BooleanOption
import dev.voidcrates.data.rewards.options.int.IntOption
import dev.voidcrates.economy.EconomyManager
import dev.voidcrates.gui.InventoryType
import dev.voidcrates.integrations.ModIntegration
import dev.voidcrates.managers.CratesManager
import dev.voidcrates.managers.CratesManager.tick
import dev.voidcrates.managers.HologramsManager
import dev.voidcrates.managers.KeyManager
import dev.voidcrates.managers.OpeningManager
import dev.voidcrates.placeholders.PlaceholderManager
import dev.voidcrates.storage.IStorage
import dev.voidcrates.storage.StorageType
import dev.voidcrates.utils.CompoundTagAdaptor
import dev.voidcrates.utils.Utils
import dev.voidcrates.utils.WebhookUtils
import kotlinx.coroutines.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import dev.voidcrates.utils.VoidCratesAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VoidCrates : ModInitializer {
    companion object {
        lateinit var INSTANCE: VoidCrates

        const val MOD_ID = "voidcrates"
        const val MOD_NAME = "VoidCrates"

        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
        val MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

        val asyncScope = CoroutineScope(Dispatchers.IO)

        @JvmStatic
        fun asResource(path: String): Identifier {
            return Identifier.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    lateinit var configDir: File
    lateinit var storage: IStorage

    lateinit var adventure: VoidCratesAudiences
    lateinit var server: MinecraftServer
    lateinit var nbtOpts: RegistryOps<Tag>

    val asyncExecutor: ExecutorService = Executors.newFixedThreadPool(8, ThreadFactoryBuilder()
        .setNameFormat("VoidCrates-Async-%d")
        .setDaemon(true)
        .build())

    @OptIn(DelicateCoroutinesApi::class)
    private val particleThreadPool = newFixedThreadPoolContext(1, "VoidCratesParticleThread")
    private val particleScope = CoroutineScope(particleThreadPool + SupervisorJob())
    fun runOnParticleThread(block: suspend () -> Unit) {
        particleScope.launch {
            block()
            delay(1)
        }
    }

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(Reward::class.java, Reward.Adapter())
        .registerTypeAdapter(Action::class.java, Action.Adapter())
        .registerTypeAdapter(StorageType::class.java, StorageType.Adapter())
        .registerTypeAdapter(GenericClickType::class.java, GenericClickType.Adapter())
        .registerTypeAdapter(ParticleEffect::class.java, ParticleEffect.Adapter())
        .registerTypeAdapter(WorldOpeningAnimation::class.java, WorldOpeningAnimation.Adapter())
        .registerTypeAdapter(IntOption::class.java, IntOption.Adapter())
        .registerTypeAdapter(BooleanOption::class.java, BooleanOption.Adapter())
        .registerTypeHierarchyAdapter(Item::class.java, Utils.RegistrySerializer(BuiltInRegistries.ITEM))
        .registerTypeHierarchyAdapter(SoundEvent::class.java, Utils.RegistrySerializer(BuiltInRegistries.SOUND_EVENT))
        .registerTypeHierarchyAdapter(ParticleOptions::class.java, Utils.CodecSerializer(ParticleTypes.CODEC))
        .registerTypeAdapter(InventoryType::class.java, InventoryType.Deserializer())
        .registerTypeAdapter(SoundOption::class.java, SoundOption.Adaptor())
        .registerTypeAdapter(CompoundTag::class.java, CompoundTagAdaptor())
        .create()

    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, MOD_ID)
        ConfigManager.load()
        this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        Lang.init()

        EconomyManager.init()

        ModIntegration.onInit()

        registerEvents()
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer ->
            this.adventure = VoidCratesAudiences(server)
            this.server = server
            this.nbtOpts = server.registryAccess().createSerializationContext(NbtOps.INSTANCE)
            ModIntegration.onServerStarting()
        })
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { _: MinecraftServer ->
            OpeningManager.load()
            CratesManager.load()
            PlaceholderManager.init()
            ModIntegration.onServerStarted()
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(dispatcher)
            KeysCommand().register(dispatcher)
        }
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerLifecycleEvents.ServerStopping { _: MinecraftServer ->
            this.storage.close()
            ModIntegration.onServerShutdown()
        })
        WebhookUtils.registerEvents()
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvents.EndTick { server ->
            tick()
            OpeningManager.tick()

            if (server.tickCount % 6000 == 0) {
                KeyManager.cleanCache()
            }
        })
    }

    fun reload() {
        this.storage.close()

        ConfigManager.load()
        this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        Lang.init()

        OpeningManager.load()
        CratesManager.load()

        if (FabricLoader.getInstance().isModLoaded("holodisplays")) HologramsManager.load()
    }
}
