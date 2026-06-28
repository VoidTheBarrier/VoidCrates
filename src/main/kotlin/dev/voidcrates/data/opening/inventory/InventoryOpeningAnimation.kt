package dev.voidcrates.data.opening.inventory

import com.google.gson.annotations.SerializedName
import dev.voidcrates.config.item.MenuItem
import dev.voidcrates.data.opening.OpeningAnimation
import dev.voidcrates.data.opening.inventory.items.SpinningItem
import dev.voidcrates.data.opening.inventory.presets.AnimatedItem
import dev.voidcrates.data.opening.inventory.presets.RewardItem
import dev.voidcrates.gui.InventoryType

class InventoryOpeningAnimation(
    val title: String,
    @SerializedName("type", alternate = ["menu_type"])
    val type: InventoryType,
    @SerializedName("close_delay")
    val closeDelay: Int,
    @SerializedName("win_slots")
    val winSlots: List<Int>,
    val skippable: Boolean = false,
    val items: Items,
    val presets: Presets
): OpeningAnimation {
    override fun instantiate(): OpeningAnimation {
        val copiedItems = Items(
            items.rewards.toMutableMap(),
            items.animated.toMutableMap(),
            items.static.toMutableMap()
        )

        val copiedPresets = Presets(
            presets.rewards.toMutableMap(),
            presets.animations.mapValues { entry -> entry.value.toMutableList() }.toMutableMap()
        )

        return InventoryOpeningAnimation(title, type, closeDelay, winSlots.toList(), skippable, copiedItems, copiedPresets)
    }

    // These are the items that are used in the inventory animation
    class Items(
        // These are items that display the rewards
        val rewards: MutableMap<String, SpinningItem>,
        // These are items that update over time
        val animated: MutableMap<String, SpinningItem>,
        // These are items that remain the same throughout the GUI
        val static: MutableMap<String, MenuItem>
    )

    class Presets(
        val rewards: MutableMap<String, RewardItem>,
        val animations: MutableMap<String, List<AnimatedItem>>,
    )
}