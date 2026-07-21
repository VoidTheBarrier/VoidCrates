package dev.voidcrates.utils

import com.mojang.serialization.JsonOps
import dev.voidcrates.VoidCrates
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.kyori.adventure.text.Component as AdventureComponent
import net.minecraft.network.chat.Component as NativeComponent

/**
 * Self-contained replacement for net.kyori.adventure.platform.modcommon.MinecraftServerAudiences.
 *
 * adventure-platform-fabric depends internally on a second module, adventure-platform-mod-shared,
 * for this functionality. That module was last independently published at 6.8.0, targeting
 * Minecraft 1.21.11 (the last obfuscated version) - its classes reference vanilla types by their
 * now-defunct intermediary names, so it cannot be used on unobfuscated 26.x. Since VoidCrates only
 * ever used three operations from it (asNative, asAdventure, all), this reimplements exactly those
 * three, built entirely on dependencies already confirmed present: vanilla's own
 * ComponentSerialization.CODEC and the bundled GsonComponentSerializer, bridged through JsonElement
 * - the same mechanism adventure-platform-mod-shared uses internally for this exact conversion.
 *
 * all()/console() logic mirrors adventure-platform-mod-shared's own MinecraftServerAudiencesImpl
 * and PlainAudience classes, read directly from source to confirm the real pattern: players get a
 * proper formatted chat message via sendSystemMessage, while the console gets a plain-text line
 * through the logger (there's no "chat window" for a log stream, so upstream strips formatting
 * there too, rather than sending a native chat Component to it).
 */
class VoidCratesAudiences(private val server: MinecraftServer) {

    private val gson = GsonComponentSerializer.gson()
    private val plainText = PlainTextComponentSerializer.plainText()

    fun asNative(component: AdventureComponent): NativeComponent {
        val json = gson.serializeToTree(component)
        return ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, json).orThrow.first
    }

    fun asAdventure(component: NativeComponent): AdventureComponent {
        val json = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component).orThrow
        return gson.deserializeFromTree(json)
    }

    /**
     * All connected players plus the console. Only sendMessage is implemented since it's the
     * only Audience method VoidCrates calls on the result of all(); every other Audience method
     * keeps its Adventure-provided no-op default.
     */
    fun all(): Audience = object : Audience {
        override fun sendMessage(source: Identity, message: AdventureComponent, type: MessageType) {
            val native = asNative(message)
            server.playerList.players.forEach { it.sendSystemMessage(native) }
            VoidCrates.LOGGER.info(plainText.serialize(message))
        }
    }

    /**
     * Wraps a single player as an Audience. Needed by third-party integrations (e.g.
     * MiniPlaceholdersService) that require a real Audience object representing a specific
     * player, now that ServerPlayer no longer implements Audience directly.
     */
    fun player(player: ServerPlayer): PlayerAudience = PlayerAudience(player, this)
}

class PlayerAudience(val player: ServerPlayer, private val audiences: VoidCratesAudiences) : Audience {
    override fun sendMessage(source: Identity, message: AdventureComponent, type: MessageType) {
        player.sendSystemMessage(audiences.asNative(message))
    }
}
