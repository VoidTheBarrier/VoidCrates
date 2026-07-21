package dev.voidcrates.data.rewards

import dev.voidcrates.data.rewards.types.CommandConsoleReward
import dev.voidcrates.data.rewards.types.CommandPlayerReward
import dev.voidcrates.data.rewards.types.ItemReward

enum class RewardType(val identifier: String, val clazz: Class<*>, val dependencies: List<String> = emptyList()) {
    COMMAND_CONSOLE("command_console", CommandConsoleReward::class.java),
    COMMAND_PLAYER("command_player", CommandPlayerReward::class.java),
    ITEM("item", ItemReward::class.java);
    // POKEMON reward temporarily disabled: Cobblemon has no Minecraft 26.x release yet.
    // Re-enable once Cobblemon ships a compatible build - see PokemonReward.kt (excluded from compilation in build.gradle.kts)

    companion object {
        fun valueOfAnyCase(name: String): RewardType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
