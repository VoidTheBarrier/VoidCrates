package dev.voidcrates.data.rewards.types

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import dev.voidcrates.VoidCrates
import dev.voidcrates.config.item.GenericItem
import dev.voidcrates.data.Crate
import dev.voidcrates.data.rewards.Reward
import dev.voidcrates.data.rewards.RewardLimits
import dev.voidcrates.data.rewards.RewardType
import dev.voidcrates.data.rewards.types.CommandConsoleReward.Companion.DEFAULT_DISPLAY
import dev.voidcrates.placeholders.PlaceholderManager
import dev.voidcrates.utils.FlexibleListAdaptorFactory
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.LevelBasedPermissionSet
import net.minecraft.server.permissions.PermissionLevel

class CommandPlayerReward(
    name: String = "",
    description: List<String> = emptyList(),
    display: GenericItem? = null,
    weight: Int = 1,
    limits: RewardLimits? = null,
    broadcast: Boolean = false,
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("commands",  alternate = ["command"])
    private val commands: List<String> = emptyList(),
    @SerializedName("permission_level")
    private val permissionLevel: Int? = null
) : Reward(RewardType.COMMAND_PLAYER, name, description, display, weight, limits, broadcast) {
    override fun giveReward(player: ServerPlayer, crate: Crate) {
        // Super to call the message
        super.giveReward(player, crate)

        val parsedCommands = commands.map { PlaceholderManager.parse(player, it) }

        var source = player.createCommandSourceStack()
        if (permissionLevel != null) {
            val pLevel = PermissionLevel.entries.getOrElse(permissionLevel) { PermissionLevel.entries.last() }
            source = source.withPermission(LevelBasedPermissionSet { pLevel })
        }

        for (command in parsedCommands) {
            VoidCrates.INSTANCE.server.commands.performPrefixedCommand(
                source,
                command
            )
        }
    }

    override fun getGenericDisplay(): GenericItem {
        return display ?: DEFAULT_DISPLAY
    }

    override fun toString(): String {
        return "CommandPlayer(type=$type, name='$name', display=$display, weight=$weight, limits=$limits, broadcast=$broadcast, commands=$commands, permissionLevel=$permissionLevel)"
    }
}
