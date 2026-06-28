package dev.voidcrates.data.opening.inventory

import dev.voidcrates.data.Crate
import dev.voidcrates.data.CrateOpenData
import dev.voidcrates.data.opening.OpeningInstance
import dev.voidcrates.data.rewards.Reward
import dev.voidcrates.gui.CrateInventory
import dev.voidcrates.utils.RandomCollection
import net.minecraft.server.level.ServerPlayer

class InventoryOpeningInstance(
    player: ServerPlayer,
    crate: Crate,
    val animation: InventoryOpeningAnimation,
    val randomBag: RandomCollection<Reward>,
    val openData: CrateOpenData,
): OpeningInstance(player, crate) {
    private val gui = CrateInventory(player, this)

    override fun setup() {
        gui.open()
    }

    override fun tick() {
        gui.tick()
    }
}
