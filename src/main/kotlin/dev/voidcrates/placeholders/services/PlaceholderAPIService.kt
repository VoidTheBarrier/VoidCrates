package dev.voidcrates.placeholders.services

import dev.voidcrates.VoidCrates
import dev.voidcrates.placeholders.IPlaceholderService
import dev.voidcrates.placeholders.PlayerPlaceholder
import dev.voidcrates.placeholders.ServerPlaceholder
import dev.voidcrates.utils.Utils
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer

class PlaceholderAPIService : IPlaceholderService {
    init {
        Utils.printInfo("PlaceholderAPI mod found! Enabling placeholder integration...")
    }

    override fun parsePlaceholders(player: ServerPlayer, text: String): String {
        return Placeholders.COMMON_PLACEHOLDER_PARSER.parseComponent(text, PlaceholderContext.of(player).asParserContext()).string
    }

    override fun registerPlayer(placeholder: PlayerPlaceholder) {
        Placeholders.registerCommon<String>(Identifier.fromNamespaceAndPath(VoidCrates.MOD_ID, placeholder.id())) { ctx, arg ->
            val player = ctx.player() as? ServerPlayer ?: return@registerCommon PlaceholderResult.invalid("NO PLAYER")
            val result = placeholder.handle(player, arg?.split(":") ?: emptyList())
            return@registerCommon if (result.isSuccessful) {
                PlaceholderResult.value(VoidCrates.INSTANCE.adventure.asNative(result.result))
            } else {
                PlaceholderResult.invalid(PlainTextComponentSerializer.plainText().serialize(result.result))
            }
        }
    }

    override fun registerServer(placeholder: ServerPlaceholder) {
        Placeholders.registerCommon<String>(Identifier.fromNamespaceAndPath(VoidCrates.MOD_ID, placeholder.id())) { _, arg ->
            val result = placeholder.handle(arg?.split(":") ?: emptyList())
            return@registerCommon if (result.isSuccessful) {
                PlaceholderResult.value(VoidCrates.INSTANCE.adventure.asNative(result.result))
            } else {
                PlaceholderResult.invalid(PlainTextComponentSerializer.plainText().serialize(result.result))
            }
        }
    }

    override fun finalizeRegister() {

    }
}
