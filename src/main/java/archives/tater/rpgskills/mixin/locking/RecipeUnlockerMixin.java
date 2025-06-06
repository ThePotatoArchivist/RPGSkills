package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import archives.tater.rpgskills.networking.RecipeBlockedPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeUnlocker.class)
public interface RecipeUnlockerMixin {
    @Inject(
            method = "shouldCraftRecipe",
            at = @At("HEAD"),
            cancellable = true)
    private void alsoCheckSkills(World world, ServerPlayerEntity player, RecipeEntry<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        var lockGroup = LockGroup.findLocked(player, recipe);
        if (lockGroup == null) {
            ServerPlayNetworking.send(player, RecipeBlockedPayload.EMPTY);
            return;
        }
        cir.setReturnValue(false);
        ServerPlayNetworking.send(player, new RecipeBlockedPayload(lockGroup));
    }
}
