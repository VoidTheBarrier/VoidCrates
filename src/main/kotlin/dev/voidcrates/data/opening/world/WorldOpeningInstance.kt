package dev.voidcrates.data.opening.world

import dev.voidcrates.data.Crate
import dev.voidcrates.data.CrateInstance
import dev.voidcrates.data.CrateOpenData
import dev.voidcrates.data.opening.OpeningInstance
import dev.voidcrates.data.rewards.Reward
import dev.voidcrates.utils.RandomCollection
import net.minecraft.server.level.ServerPlayer

class WorldOpeningInstance(
    player: ServerPlayer,
    crate: Crate,
    val instance: CrateInstance,
    val animation: WorldOpeningAnimation,
    val randomBag: RandomCollection<Reward>,
    val openData: CrateOpenData,
): OpeningInstance(player, crate) {
    override fun tick() {
        animation.tick(this)
    }

    override fun setup() {
        animation.setup(this)
    }

    override fun stop() {
        super.stop()
        animation.stop(this)
    }
}
