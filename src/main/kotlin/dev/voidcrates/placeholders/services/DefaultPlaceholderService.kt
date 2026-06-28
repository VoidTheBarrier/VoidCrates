package dev.voidcrates.placeholders.services

import dev.voidcrates.placeholders.IPlaceholderService
import dev.voidcrates.placeholders.PlayerPlaceholder
import dev.voidcrates.placeholders.ServerPlaceholder
import net.minecraft.server.level.ServerPlayer

class DefaultPlaceholderService : IPlaceholderService {
    override fun parsePlaceholders(player: ServerPlayer, text: String): String {
        return text
            .replace("%player%", player.name.string)
            .replace("%player_uuid%", player.uuid.toString())
    }

    override fun registerPlayer(placeholder: PlayerPlaceholder) {

    }

    override fun registerServer(placeholder: ServerPlaceholder) {

    }

    override fun finalizeRegister() {

    }
}
