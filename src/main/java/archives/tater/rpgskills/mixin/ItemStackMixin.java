package archives.tater.rpgskills.mixin;

import archives.tater.rpgskills.data.LockedItems;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @WrapOperation(
            method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;")
    )
    private ActionResult checkLock(Item instance, ItemUsageContext context, Operation<ActionResult> original) {
        var player = context.getPlayer();
        return player != null && LockedItems.isItemLocked(instance, player) ? ActionResult.PASS : original.call(instance, context);
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;")
    )
    private TypedActionResult<ItemStack> checkLock(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
        return LockedItems.isItemLocked(instance, user) ? TypedActionResult.pass(user.getStackInHand(hand)) : original.call(instance, world, user, hand);
    }

    @ModifyExpressionValue(
            method = "getTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z")
    )
    private boolean addTooltip(boolean original, @Local(argsOnly = true) PlayerEntity player) {
        return original && !LockedItems.isItemLocked(getItem(), player);
    }
}
