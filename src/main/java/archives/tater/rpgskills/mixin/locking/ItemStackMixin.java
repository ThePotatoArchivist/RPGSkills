package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.ItemLockTooltip;
import archives.tater.rpgskills.data.LockGroup;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings({"deprecation"})
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean canPlaceOn(Registry<Block> blockRegistry, CachedBlockPosition pos);

    @WrapOperation(
            method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;")
    )
    private ActionResult checkLock(Item instance, ItemUsageContext context, Operation<ActionResult> original) {
        var player = context.getPlayer();
        if (player == null || !LockGroup.isLocked(player, this)) {
            return original.call(instance, context);
        }
        player.sendMessage(LockGroup.messageOf(player, this), true);
        return ActionResult.CONSUME;
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;")
    )
    private TypedActionResult<ItemStack> checkLock(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
        if (!LockGroup.isLocked(user, this)) {
            return original.call(instance, world, user, hand);
        }
        user.sendMessage(LockGroup.messageOf(user, this), true);
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Inject(
            method = "getTooltip",
            at = @At("TAIL")
    )
    private void addTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> tooltip) {
        ItemLockTooltip.appendTooltip((ItemStack) (Object) this, player, tooltip, context);
    }

    @ModifyExpressionValue(
            method = "getTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;")
    )
    private Text modifyName(Text original, @Local(argsOnly = true) PlayerEntity player) {
        return player != null && LockGroup.isLocked(player, this) ? LockGroup.nameOf(player, this, original) : original;
    }
}
