package dev.voidcrates.data.actions.types

import dev.voidcrates.data.actions.Action
import dev.voidcrates.data.actions.ActionType
import dev.voidcrates.gui.PreviewInventory
import dev.voidcrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer

class NextPage(
    val wrap: Boolean = false
) : Action(ActionType.NEXT_PAGE) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) $this")

        if (gui !is PreviewInventory) {
            Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) tried to execute a NextPage action not paginated.")
            return
        }

        gui.nextPage(wrap)
    }

    override fun toString(): String {
        return "NextPage(wrap=$wrap)"
    }
}
