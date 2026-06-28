package dev.voidcrates.config

import com.google.gson.annotations.SerializedName
import dev.voidcrates.config.item.ActionMenuItem
import dev.voidcrates.config.item.KeyMenuItem
import dev.voidcrates.gui.InventoryType

class KeysMenu(
    val title: String = "Keys",
    @SerializedName("type", alternate = ["menu_type"])
    val type: InventoryType = InventoryType.GENERIC_9x3,
    val keys: MutableMap<String, KeyMenuItem> = mutableMapOf(),
    val items: MutableMap<String, ActionMenuItem> = mutableMapOf()
) {
    override fun toString(): String {
        return "KeysMenu(title='$title', type=$type, keys=$keys, items=$items)"
    }
}