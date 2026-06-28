package dev.voidcrates.economy

import dev.voidcrates.economy.services.BEconomyService
import dev.voidcrates.economy.services.CobbleDollarsEconomyService
import dev.voidcrates.economy.services.ImpactorEconomyService
import dev.voidcrates.economy.services.PebblesEconomyService
import net.fabricmc.loader.api.FabricLoader

enum class InternalEconomyTypes(
    val identifier: String,
    val modId: String,
    val clazz: Class<out IEconomyService>
) {
    IMPACTOR("impactor", "impactor", ImpactorEconomyService::class.java),
    PEBBLES("pebbles", "pebbles-economy", PebblesEconomyService::class.java),
    COBBLEDOLLARS("cobbledollars", "cobbledollars", CobbleDollarsEconomyService::class.java),
    BECONOMY("beconomy", "beconomy", BEconomyService::class.java);

    fun isModPresent() : Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }
}
