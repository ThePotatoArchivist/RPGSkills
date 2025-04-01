package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.networking.RecipeBlockedPayload;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    @ModifyExpressionValue(
            method = "updateResult",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z")
    )
    private static boolean clearBlocked(boolean original, @Local ServerPlayerEntity player) {
        if (!original)
            ServerPlayNetworking.send(player, RecipeBlockedPayload.EMPTY);
        return original;
    }
}
