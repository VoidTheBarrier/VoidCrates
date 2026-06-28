package dev.voidcrates.data.actions.types

import com.google.gson.annotations.JsonAdapter
import dev.voidcrates.VoidCrates
import dev.voidcrates.data.actions.Action
import dev.voidcrates.data.actions.ActionType
import dev.voidcrates.utils.FlexibleListAdaptorFactory
import dev.voidcrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer

class CommandConsole(
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val commands: List<String> = emptyList()
) : Action(ActionType.COMMAND_CONSOLE) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        val parsedCommands = commands.map { it /* TODO: do parsing */ }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Commands($parsedCommands): $this")

        for (command in parsedCommands) {
            VoidCrates.INSTANCE.server.commands.performPrefixedCommand(
                VoidCrates.INSTANCE.server.createCommandSourceStack(),
                command
            )
        }
    }

    override fun toString(): String {
        return "CommandConsole(commands=$commands)"
    }
}
