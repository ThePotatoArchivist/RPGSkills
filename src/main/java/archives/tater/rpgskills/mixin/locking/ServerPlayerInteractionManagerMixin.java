package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(
            method = "tryBreakBlock",
            at = @At("HEAD"),
            cancellable = true)
    private void checkLock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var lockGroup = LockGroup.findLocked(player, player.getMainHandStack());
        if (lockGroup == null) return;
        cir.setReturnValue(false);
        player.sendMessage(lockGroup.itemMessage(), true);
    }
}
