package dev.voidcrates.data.actions.types

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import dev.voidcrates.VoidCrates
import dev.voidcrates.data.actions.Action
import dev.voidcrates.data.actions.ActionType
import dev.voidcrates.utils.FlexibleListAdaptorFactory
import dev.voidcrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.LevelBasedPermissionSet
import net.minecraft.server.permissions.PermissionLevel

class CommandPlayer(
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val commands: List<String> = emptyList(),
    @SerializedName("permission_level")
    private val permissionLevel: Int? = null
) : Action(ActionType.COMMAND_PLAYER) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        val parsedCommands = commands.map { it /* TODO: do parsing */ }

        var source = player.createCommandSourceStack()
        if (permissionLevel != null) {
            val pLevel = PermissionLevel.entries.getOrElse(permissionLevel) { PermissionLevel.entries.last() }
            source = source.withPermission(LevelBasedPermissionSet { pLevel })
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Commands($parsedCommands): $this")

        for (command in parsedCommands) {
            VoidCrates.INSTANCE.server.commands.performPrefixedCommand(
                source,
                command
            )
        }
    }

    override fun toString(): String {
        return "CommandPlayer(commands=$commands, permissionLevel=$permissionLevel)"
    }
}
