package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import archives.tater.rpgskills.networking.RecipeBlockedPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(RecipeUnlocker.class)
public interface RecipeUnlockerMixin {
    @Inject(
            method = "shouldCraftRecipe",
            at = @At("HEAD"),
            cancellable = true)
    private void alsoCheckSkills(World world, ServerPlayerEntity player, Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (LockGroup.isLocked(player, recipe.getId())) {
            cir.setReturnValue(false);
            var lockGroup = LockGroup.of(player, recipe.getId());
            if (lockGroup != null && lockGroup.getKey().isPresent()) {
                ServerPlayNetworking.send(player, new RecipeBlockedPacket(lockGroup.getKey().get()));
                return;
            }
        }
        ServerPlayNetworking.send(player, RecipeBlockedPacket.EMPTY);
    }
}
