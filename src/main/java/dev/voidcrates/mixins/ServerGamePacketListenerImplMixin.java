package dev.voidcrates.mixins;

import dev.voidcrates.data.CrateOpenData;
import dev.voidcrates.data.DimensionalBlockPos;
import dev.voidcrates.events.CrateInteractionEvent;
import dev.voidcrates.events.ItemSwingEvent;
import dev.voidcrates.managers.CratesManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleAnimate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V", shift = At.Shift.AFTER))
    private void voidcrates$onHandleAnimate(ServerboundSwingPacket packet, CallbackInfo ci) {
        var itemStack = this.player.getItemInHand(packet.getHand());
        if (itemStack.isEmpty()) return;
        var result = this.player.pick(player.blockInteractionRange(), 1.0F, false);
        if (!(result instanceof BlockHitResult) || result.getType() != HitResult.Type.MISS) {
            return;
        }

        ItemSwingEvent.EVENT.invoker().interact(player, itemStack, packet.getHand());
    }

    /**
     * Intercept left-click block packets at the lowest possible level so that
     * protection mods (YAWP, etc.) and adventure mode cannot suppress the event
     * before VoidCrates sees it. We cancel further processing only when the
     * targeted block is a registered crate, which also prevents double-firing
     * via the AttackBlockCallback handler in CratesManager.
     */
    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void voidcrates$onHandlePlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (packet.getAction() != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return;
        if (!(this.player.level() instanceof ServerLevel serverLevel)) return;

        BlockPos blockPos = packet.getPos();
        DimensionalBlockPos dimensionalPos = new DimensionalBlockPos(
            serverLevel.dimension().identifier().toString(),
            blockPos.getX(),
            blockPos.getY(),
            blockPos.getZ()
        );

        var instance = CratesManager.INSTANCE.getCrateFromPos(dimensionalPos);
        if (instance == null) return;

        CrateInteractionEvent.InteractionType interactionType = this.player.isShiftKeyDown()
            ? CrateInteractionEvent.InteractionType.SHIFT_LEFT_CLICK
            : CrateInteractionEvent.InteractionType.LEFT_CLICK;

        CrateInteractionEvent.EVENT.invoker().interact(
            this.player,
            instance.getCrate(),
            new CrateOpenData(dimensionalPos, null, interactionType)
        );

        // Cancel the rest of handlePlayerAction so:
        // 1. The block doesn't get damaged/broken
        // 2. AttackBlockCallback isn't fired (preventing double-trigger)
        ci.cancel();
    }
}
