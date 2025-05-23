package archives.tater.rpgskills.mixin.client;

import archives.tater.rpgskills.LockEventsKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftClient.class, priority = 800) // override better combat
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow @Final public GameOptions options;

    @Inject(
            method = "doAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventAttack(CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        if (LockEventsKt.checkAttackLocked(player))
            cir.setReturnValue(false);
    }

    @Inject(
            method = "handleBlockBreaking",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventAttack(boolean breaking, CallbackInfo ci) {
        if (player == null || !options.attackKey.isPressed()) return;
        if (LockEventsKt.checkAttackLocked(player))
            ci.cancel();
    }
}
