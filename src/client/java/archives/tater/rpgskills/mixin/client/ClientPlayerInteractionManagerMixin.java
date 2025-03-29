package archives.tater.rpgskills.mixin.client;

import archives.tater.rpgskills.data.LockGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(
            method = "attackBlock",
            at = @At("HEAD"),
            cancellable = true)
    private void checkLock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (client.player == null || !LockGroup.isLocked(client.player, client.player.getMainHandStack())) return;
        cir.setReturnValue(false);
    }
}
