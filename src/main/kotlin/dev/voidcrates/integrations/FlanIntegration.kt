package dev.voidcrates.integrations

import dev.voidcrates.data.DimensionalBlockPos
import dev.voidcrates.managers.CratesManager
import dev.voidcrates.utils.Utils
import io.github.flemmli97.flan.api.fabric.PermissionCheckEvent
import io.github.flemmli97.flan.api.permission.BuiltinPermission
import net.minecraft.world.InteractionResult

class FlanIntegration: IntegratedMod {
    override fun onServerStarted() {
        Utils.printInfo("The mod Flans was found, enabling integrations...")
        PermissionCheckEvent.CHECK.register { player, rs, pos ->
            if (!rs.equals(BuiltinPermission.BREAK)) return@register InteractionResult.PASS
            val dimensionalPos = DimensionalBlockPos(player.level().dimension().identifier().toString(), pos.x, pos.y, pos.z)
            CratesManager.getCrateFromPos(dimensionalPos)?.let { crate ->
                return@register InteractionResult.SUCCESS
            }
            return@register InteractionResult.PASS
        }
    }
}