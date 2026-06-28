package dev.voidcrates.data.rewards.types

import com.google.gson.annotations.JsonAdapter
import dev.voidcrates.VoidCrates
import dev.voidcrates.config.item.GenericItem
import dev.voidcrates.data.Crate
import dev.voidcrates.data.rewards.Reward
import dev.voidcrates.data.rewards.RewardLimits
import dev.voidcrates.data.rewards.RewardType
import dev.voidcrates.placeholders.PlaceholderManager
import dev.voidcrates.utils.FlexibleListAdaptorFactory
import dev.voidcrates.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CommandConsoleReward(
    name: String = "",
    description: List<String> = emptyList(),
    weight: Int = 1,
    display: GenericItem? = null,
    limits: RewardLimits? = null,
    broadcast: Boolean = false,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val commands: List<String> = emptyList()
) : Reward(RewardType.COMMAND_CONSOLE, name, description, display, weight, limits, broadcast) {
    companion object {
        val DEFAULT_DISPLAY = GenericItem("minecraft:paper", name = "Command")
    }

    override fun giveReward(player: ServerPlayer, crate: Crate) {
        // Super to call the message
        super.giveReward(player, crate)

        if (VoidCrates.INSTANCE.server.commands == null) {
            Utils.printError("There was an error while giving a reward for player ${player.name}: Server was somehow null on command execution?")
            return
        }

        for (command in commands) {
            VoidCrates.INSTANCE.server.commands.performPrefixedCommand(
                VoidCrates.INSTANCE.server.createCommandSourceStack(),
                PlaceholderManager.parse(player, command)
            )
        }
    }

    override fun getGenericDisplay(): GenericItem {
        return display ?: DEFAULT_DISPLAY
    }

    override fun toString(): String {
        return "CommandConsole(type=$type, name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast, commands=$commands)"
    }
}
