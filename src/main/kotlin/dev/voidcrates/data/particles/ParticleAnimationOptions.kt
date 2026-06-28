package dev.voidcrates.data.particles

import dev.voidcrates.data.CrateInstance
import dev.voidcrates.data.particles.effects.ParticleEffect

class ParticleAnimationOptions(
    val mode: AnimationMode,
    val distance: Double,
    val effects: List<ParticleEffect>
) {
    fun generateAnimation(instance: CrateInstance): ParticleAnimation {
        val animation = ParticleAnimation()

        animation.setMode(mode)
        animation.setDistance(distance)

        for (effect in effects) {
            animation.addTimeline(effect.generateTimeline(instance))
        }

        return animation
    }
}