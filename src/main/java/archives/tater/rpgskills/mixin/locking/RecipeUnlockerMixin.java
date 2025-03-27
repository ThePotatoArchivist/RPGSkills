package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import net.minecraft.recipe.Recipe;
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
    private void alsoCheckSkills(World world, ServerPlayerEntity player, Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (LockGroup.isLocked(player, recipe.getId())) {
            cir.setReturnValue(false);
        }
    }
}
