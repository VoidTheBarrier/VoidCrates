package dev.voidcrates.data.actions.types

import dev.voidcrates.data.actions.Action
import dev.voidcrates.data.actions.ActionType
import dev.voidcrates.utils.Utils
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.core.Holder
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

class PlaySound(
    type: ActionType = ActionType.PLAY_SOUND,
    private val sound: String = "",
    private val source: String? = null,
    private val volume: Float = 1.0F,
    private val pitch: Float = 1.0F
) : Action(type) {
    override fun executeAction(player: ServerPlayer, gui: SimpleGui) {
        if (sound.isEmpty()) {
            Utils.printError("[ACTION - ${type.name}] There was an error while executing for player ${player.name}: Sound ID was empty")
            return
        }

        val soundEvent = SoundEvent.createVariableRangeEvent(Identifier.parse(sound))

        var category = if (source == null) SoundSource.MASTER else SoundSource.entries.firstOrNull { it.name.equals(source, true) }
        if (category == null) {
            Utils.printError("[ACTION - ${type.name}] There was an error while executing for player ${player.name}: Sound Source '$source' was not found, defaulting to MASTER")
            category = SoundSource.MASTER
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), SoundEvent($soundEvent), Category($category): $this")

        if (!player.server.isStopped) {
            player.server.executeIfPossible {
                player.connection.send(
                    ClientboundSoundPacket(
                        Holder.direct(soundEvent),
                        category,
                        player.x, player.y, player.z,
                        volume, pitch, 0L
                    )
                )
            }
        }
    }

    override fun toString(): String {
        return "PlaySound(sound='$sound', source=$source, volume=$volume, pitch=$pitch)"
    }
}
