package dev.voidcrates.data.opening

import dev.voidcrates.data.Crate
import dev.voidcrates.managers.OpeningManager
import net.minecraft.server.level.ServerPlayer

abstract class OpeningInstance(
    val player: ServerPlayer,
    val crate: Crate,
) {
    abstract fun setup()

    abstract fun tick()

    open fun stop() {
        destroy()
    }

    open fun destroy() {
        OpeningManager.removeInstance(player.uuid)
    }

    override fun toString(): String {
        return "OpeningInstance(player=$player, crate=$crate)"
    }
}