package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import archives.tater.rpgskills.networking.UiActionBlockedPayload;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    @WrapOperation(
            method = "updateResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;shouldCraftRecipe(Lnet/minecraft/world/World;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/RecipeEntry;)Z")
    )
    private static boolean alsoCheckSkills(CraftingResultInventory instance, World world, ServerPlayerEntity player, RecipeEntry<?> recipe, Operation<Boolean> original) {
        var lockGroup = LockGroup.findLocked(player, recipe);
        if (lockGroup == null) {
            ServerPlayNetworking.send(player, UiActionBlockedPayload.EMPTY);
            return original.call(instance, world, player, recipe);
        }
        ServerPlayNetworking.send(player, new UiActionBlockedPayload(lockGroup));
        return false;
    }

    @ModifyExpressionValue(
            method = "updateResult",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z")
    )
    private static boolean clearBlocked(boolean original, @Local ServerPlayerEntity player) {
        if (!original)
            ServerPlayNetworking.send(player, UiActionBlockedPayload.EMPTY);
        return original;
    }
}
