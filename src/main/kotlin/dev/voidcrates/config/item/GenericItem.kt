package dev.voidcrates.config.item

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import dev.voidcrates.VoidCrates
import dev.voidcrates.placeholders.PlaceholderManager
import dev.voidcrates.utils.FlexibleListAdaptorFactory
import dev.voidcrates.utils.Utils
import dev.voidcrates.utils.asNative
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

open class GenericItem(
    val item: String = "",
    val amount: Int = 1,
    var name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String>? = null,
    @SerializedName("components", alternate = ["nbt"])
    val components: CompoundTag? = null,
    @SerializedName("custom_model_data")
    val customModelData: Int? = null,
) {
    fun createItemStack(player: ServerPlayer, placeholders: Map<String, String> = emptyMap()): ItemStack {
        val stack = getBaseItem(player, placeholders) ?: return ItemStack(Items.AIR, amount)

        if (components != null) {
            // Parses the nbt and attempts to replace any placeholders
            val nbtCopy = components.copy()
            for (key in components.keySet()) {
                val element = components.get(key)
                if (element != null) {
                    if (element is StringTag) {
                        nbtCopy.putString(key, element.asString().orElse(""))
                    } else if (element is ListTag) {
                        val parsed = ListTag()
                        for (entry in element) {
                            if (entry is StringTag) {
                                parsed.add(StringTag.valueOf(entry.asString().orElse("")))
                            } else {
                                parsed.add(entry)
                            }
                        }
                        nbtCopy.put(key, parsed)
                    }
                }
            }

            DataComponentPatch.CODEC.decode(VoidCrates.INSTANCE.nbtOpts, nbtCopy).result().ifPresent { result ->
                stack.applyComponents(result.first)
            }
        }

        val dataComponents = DataComponentPatch.builder()

        if (customModelData != null) {
            dataComponents.set(DataComponents.CUSTOM_MODEL_DATA,
                CustomModelData(listOf(customModelData.toFloat()), listOf(), listOf(), listOf()))
        }

        name?.let { name ->
            dataComponents.set(
                DataComponents.ITEM_NAME, Component.empty().setStyle(Style.EMPTY.withItalic(false))
                    .append(name.asNative(player, placeholders)))
        }

        if (!lore.isNullOrEmpty()) {
            val parsedLore: MutableList<String> = mutableListOf()
            for (line in lore.stream().map { it }.toList()) {
                val parsedLine = PlaceholderManager.parse(player, line, placeholders)
                if (parsedLine.contains("\n")) {
                    parsedLine.split("\n").forEach { parsedLore.add(it) }
                } else {
                    parsedLore.add(parsedLine)
                }
            }
            dataComponents.set(DataComponents.LORE, ItemLore(parsedLore.stream().map {
                Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(it.asNative()) as Component
            }.toList()))
        }

        stack.applyComponents(dataComponents.build())

        return stack
    }

    private fun getBaseItem(player: ServerPlayer, placeholders: Map<String, String> = emptyMap()): ItemStack? {
        if (item.isEmpty()) return null

        val parsedItem = PlaceholderManager.parse(player, item, placeholders)

        // Handles player head parsing
        if (parsedItem.startsWith("playerhead", true)) {
            val headStack = ItemStack(Items.PLAYER_HEAD, amount)

            var uuid: UUID? = null
            if (parsedItem.contains("-")) {
                val arg = parsedItem.replace("playerhead-", "")
                if (arg.isNotEmpty()) {
                    if (arg.contains("-")) {
                        // CASE: UUID format
                        try {
                            uuid = UUID.fromString(arg)
                        } catch (_: Exception) {}
                    } else if (arg.length <= 16) {
                        // CASE: Player name format
                        val targetPlayer = VoidCrates.INSTANCE.server.playerList?.getPlayerByName(arg)
                        if (targetPlayer != null) {
                            uuid = targetPlayer.uuid
                        }
                    } else {
                        // CASE: Game Profile format
                        val gameProfile = GameProfile(UUID.randomUUID(), "")
                        gameProfile.properties.put("textures", Property("textures", arg))
                        headStack.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile))
                            .build())
                        return headStack
                    }
                }
            } else {
                // CASE: Only "playerhead" is provided, use the viewing player's UUID
                uuid = player.uuid
            }

            if (uuid != null) {
                // Check if the player is currently online for immediate full-profile resolution
                val onlinePlayer = VoidCrates.INSTANCE.server.playerList?.getPlayer(uuid)
                if (onlinePlayer != null) {
                    headStack.applyComponents(DataComponentPatch.builder()
                        .set(DataComponents.PROFILE, ResolvableProfile.createResolved(onlinePlayer.gameProfile))
                        .build())
                    return headStack
                }
                // For offline players: embed the UUID so the client can resolve the skin lazily
                val offlineProfile = GameProfile(uuid, "")
                headStack.applyComponents(DataComponentPatch.builder()
                    .set(DataComponents.PROFILE, ResolvableProfile.createResolved(offlineProfile))
                    .build())
                return headStack
            }

            Utils.printError("Error while attempting to parse Player Head: $parsedItem")
            return headStack
        }

        val newItem = BuiltInRegistries.ITEM.getOptional(Identifier.parse(parsedItem))
        if (newItem.isEmpty()) {
            Utils.printError("Error while getting Item, defaulting to AIR: $parsedItem")
            return ItemStack(Items.AIR, amount)
        }

        return ItemStack(newItem.get(), amount)
    }

    fun copy(): GenericItem {
        return GenericItem(item, amount, name, lore, components, customModelData)
    }

    override fun toString(): String {
        return "GenericItem(item=$item, amount=$amount, name=$name, lore=$lore, components=$components, customModelData=$customModelData)"
    }
}