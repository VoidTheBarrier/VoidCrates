package dev.voidcrates.placeholders.type.player

import dev.voidcrates.managers.KeyManager
import dev.voidcrates.placeholders.GenericResult
import dev.voidcrates.placeholders.PlayerPlaceholder
import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerPlayer

class PlayerKeys : PlayerPlaceholder {
    override fun handle(player: ServerPlayer, args: List<String>): GenericResult {
        val keyId = args.firstOrNull() ?: return GenericResult.invalid(Component.text("Key ID Required"))
        return GenericResult.valid(Component.text(KeyManager.getCachedKeys(player.uuid, keyId).toString()))
    }

    override fun id(): String = "keys"
}
