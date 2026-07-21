package dev.voidcrates.data

import dev.voidcrates.VoidCrates
import dev.voidcrates.config.block.HologramOptions
import dev.voidcrates.config.block.ModelOptions
import dev.voidcrates.data.particles.ParticleAnimation
import dev.voidcrates.data.particles.ParticleAnimationOptions
import dev.voidcrates.managers.HologramsManager
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

class CrateInstance(
    val crate: Crate,
    val level: ServerLevel,
    val pos: BlockPos,
    val dimPos: DimensionalBlockPos,
    val model: ModelOptions? = null,
    val hologram: HologramOptions? = null,
    val particles: ParticleAnimationOptions? = null,
) {
    private var particleAnimation: ParticleAnimation? = null

    private var nearPlayers = mutableSetOf<ServerPlayer>()
    private var ticks = 0

    init {
        particleAnimation = particles?.generateAnimation(this)
    }

    fun destroy() {
        if (FabricLoader.getInstance().isModLoaded("holodisplays")) {
            HologramsManager.unloadCrateHologram(this)
        }
    }

    fun tick() {
        particleAnimation?.let { particle ->
            VoidCrates.INSTANCE.runOnParticleThread {
                if (ticks++ >= 20) {
                    ticks = 0
                    nearPlayers = level.players().filter { p ->
                        p.distanceToSqr(Vec3.atBottomCenterOf(pos)) < particle.getDistance()
                    }.toMutableSet()
                }

                particle.tick(nearPlayers.toList())
            }
        }
    }
}