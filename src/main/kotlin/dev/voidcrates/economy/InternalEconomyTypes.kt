package dev.voidcrates.economy

import dev.voidcrates.economy.services.BEconomyService
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
    BECONOMY("beconomy", "beconomy", BEconomyService::class.java);
    // COBBLEDOLLARS temporarily disabled: CobbleDollars depends on Cobblemon, which has no
    // Minecraft 26.x release yet. Re-enable once both mods ship compatible builds - see
    // CobbleDollarsEconomyService.kt (excluded from compilation in build.gradle.kts)

    fun isModPresent() : Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }
}
