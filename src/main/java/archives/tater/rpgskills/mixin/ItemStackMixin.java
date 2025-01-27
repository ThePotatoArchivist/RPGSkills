package archives.tater.rpgskills.mixin;

import archives.tater.rpgskills.data.LockCategories;
import archives.tater.rpgskills.data.LockedItems;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("deprecation")
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean hasCustomName();

    @WrapOperation(
            method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;")
    )
    private ActionResult checkLock(Item instance, ItemUsageContext context, Operation<ActionResult> original) {
        var player = context.getPlayer();
        if (player == null || !LockedItems.isItemLocked(this, player)) {
            return original.call(instance, context);
        }
        player.sendMessage(Text.translatable(LockCategories.lockTranslationOf(this, "message")), true);
        return ActionResult.CONSUME;
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;")
    )
    private TypedActionResult<ItemStack> checkLock(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
        if (!LockedItems.isItemLocked(this, user)) {
            return original.call(instance, world, user, hand);
        }
        user.sendMessage(Text.translatable(LockCategories.lockTranslationOf(this, "message")), true);
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @ModifyExpressionValue(
            method = "getTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z")
    )
    private boolean addTooltip(boolean original, @Local(argsOnly = true) PlayerEntity player) {
        return original && !LockedItems.isItemLocked(this, player);
    }

    @ModifyExpressionValue(
            method = "getTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;")
    )
    private Text modifyName(Text original, @Local(argsOnly = true) PlayerEntity player) {
        return LockCategories.lockProcessName(this, player, original);
    }
}
