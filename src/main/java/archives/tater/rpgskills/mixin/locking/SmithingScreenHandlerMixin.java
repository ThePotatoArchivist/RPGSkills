package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import archives.tater.rpgskills.networking.UiActionBlockedPayload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.SmithingTrimRecipe;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;

import org.jetbrains.annotations.Nullable;

import java.util.List;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin extends ForgingScreenHandler {
    public SmithingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(
            method = "updateResult",
            at = @At("HEAD")
    )
    private void resetLocked(CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer)
            ServerPlayNetworking.send(serverPlayer, new UiActionBlockedPayload((LockGroup) null));
    }

    @WrapOperation(
            method = "updateResult",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z")
    )
    private boolean checkLocked(List<RecipeEntry<SmithingRecipe>> instance, Operation<Boolean> original) {
        if (original.call(instance) || instance.isEmpty()) return true; // Just in case someone messes with the check before
        var recipe = instance.getFirst();
        if (recipe.value() instanceof SmithingTrimRecipe) return false;
        var lockGroup = LockGroup.findLocked(player, recipe);
        if (lockGroup == null) return false;
        if (player instanceof ServerPlayerEntity serverPlayer)
            ServerPlayNetworking.send(serverPlayer, new UiActionBlockedPayload(lockGroup));
        return true;
    }
}
