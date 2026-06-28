package dev.voidcrates.data.previews

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import dev.voidcrates.config.item.ActionMenuItem
import dev.voidcrates.data.Crate
import dev.voidcrates.data.rewards.Reward
import dev.voidcrates.data.userdata.UserData
import dev.voidcrates.gui.InventoryType
import dev.voidcrates.utils.FlexibleListAdaptorFactory
import dev.voidcrates.utils.asNative
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

class Preview(
    val title: String,
    @SerializedName("type", alternate = ["menu_type"])
    val type: InventoryType,
    val rewards: RewardButton,
    val items: MutableMap<String, ActionMenuItem>,
) {
    class RewardButton(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val slots: List<Int> = emptyList(),
        val name: String? = null,
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val lore: List<String> = emptyList()
    ) {
        fun createItemStack(player: ServerPlayer, reward: Reward, crate: Crate, userData: UserData): ItemStack {
            val placeholders = reward.getPlaceholders(userData, crate).toMutableMap()

            // Either get the preview override item from the reward's "preview" option, or create a default display item
            val item: ItemStack = reward.preview?.let { guiItem ->
                return@let guiItem.createItemStack(player, placeholders)
            } ?: reward.getDisplayItem(player, placeholders)

            val dataComponents = DataComponentPatch.builder()

            // Apply name and lore overrides or defaults, if either are defined
            (reward.preview?.name ?: name)?.let { name ->
                dataComponents.set(
                    DataComponents.ITEM_NAME, Component.empty().setStyle(Style.EMPTY.withItalic(false))
                        .append(
                            name.let {
                                placeholders.entries.fold(it) { acc, (key, value) ->
                                    acc.replace(key, value)
                                }
                            }.asNative()
                        ))
            }

            (reward.preview?.lore ?: lore).let { lore ->
                if (lore.isNotEmpty()) {
                    val parsedLore: MutableList<String> = mutableListOf()
                    for (line in lore.stream().map { it }.toList()) {
                        val parsedLine = line.let {
                            placeholders.entries.fold(it) { acc, (key, value) ->
                                acc.replace(key, value)
                            }
                        }

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
            }

            item.applyComponents(dataComponents.build())

            return item
        }
    }
}
