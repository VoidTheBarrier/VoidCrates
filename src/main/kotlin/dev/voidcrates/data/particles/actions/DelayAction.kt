package dev.voidcrates.data.particles.actions

import dev.voidcrates.data.particles.AnimationAction
import net.minecraft.server.level.ServerPlayer

class DelayAction(val delay: Int): AnimationAction() {
    private var ticks = 0

    override fun tick(players: List<ServerPlayer>) {
        ticks++
    }

    override fun isComplete(): Boolean {
        return ticks >= delay
    }

    override fun reset() {
        ticks = 0
    }

    override fun length(): Int {
        return delay
    }
}