package dev.voidcrates.data.actions.types

import com.google.gson.annotations.JsonAdapter
import dev.voidcrates.VoidCrates
import dev.voidcrates.data.actions.Action
import dev.voidcrates.data.actions.ActionType
import dev.voidcrates.placeholders.PlaceholderManager
import dev.voidcrates.utils.FlexibleListAdaptorFactory
import dev.voidcrates.utils.Utils
import dev.voidcrates.utils.asAdventure
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer

class MessageBroadcast(
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val message: List<String> = emptyList()
) : Action(ActionType.BROADCAST) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        val parsedMessages = message.map { PlaceholderManager.parse(player, it) }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Messages($parsedMessages): $this")

        for (line in parsedMessages) {
            VoidCrates.INSTANCE.adventure.all().sendMessage(line.asAdventure())
        }
    }

    override fun toString(): String {
        return "MessageBroadcast(message=$message)"
    }
}
