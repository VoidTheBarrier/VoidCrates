package dev.voidcrates.integrations.holodisplays

import dev.voidcrates.data.CrateInstance
import java.util.*

class CrateHologramData(
    val instance: CrateInstance,
    val hiddenPlayers: MutableList<UUID> = mutableListOf()
)