package dev.voidcrates.config.block

import dev.voidcrates.data.DimensionalBlockPos

class CrateBlockLocation(
    val dimension: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val model: ModelOptions? = null, // Per location override
    val hologram: HologramOptions? = null, // Per location override
    val particles: String? = null, // Per location override
) {
    fun getDimensionalBlockPos(): DimensionalBlockPos {
        return DimensionalBlockPos(dimension, x, y, z)
    }

    fun equalsDimBlockPos(other: DimensionalBlockPos): Boolean {
        return dimension.equals(other.dimension, true) && x == other.x && y == other.y && z == other.z
    }
}