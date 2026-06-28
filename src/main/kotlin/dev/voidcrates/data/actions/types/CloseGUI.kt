package dev.voidcrates.data.actions.types

import dev.voidcrates.data.actions.Action
import dev.voidcrates.data.actions.ActionType
import dev.voidcrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer

class CloseGUI(
) : Action(ActionType.CLOSE_GUI) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}): $this")
        gui.close()
    }

    override fun toString(): String {
        return "CloseGUI()"
    }
}
