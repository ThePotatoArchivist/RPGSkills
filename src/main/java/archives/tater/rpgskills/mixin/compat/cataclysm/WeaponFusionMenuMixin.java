package archives.tater.rpgskills.mixin.compat.cataclysm;

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
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

import com.github.L_Ender.cataclysm.inventory.WeaponfusionMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Yes these are mixins into a neoforge mod from a fabric mod. I don't really have a better option.
@SuppressWarnings("MixinSuperClass")
@Mixin(value = WeaponfusionMenu.class, remap = false)
public abstract class WeaponFusionMenuMixin extends ForgingScreenHandler {

    public WeaponFusionMenuMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(
            method = "createResult",
            at = @At("HEAD")
    )
    private void resetLocked(CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer)
            ServerPlayNetworking.send(serverPlayer, new UiActionBlockedPayload((LockGroup) null));
    }

    @WrapOperation(
            method = "createResult",
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
