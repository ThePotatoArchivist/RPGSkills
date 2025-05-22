package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.ItemLockTooltip;
import archives.tater.rpgskills.data.LockGroup;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapOperation(
            method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;")
    )
    private ActionResult checkLock(Item instance, ItemUsageContext context, Operation<ActionResult> original) {
        var player = context.getPlayer();
        if (player == null) return original.call(instance, context);
        var lockGroup = LockGroup.findLocked(player, (ItemStack) (Object) this);
        if (lockGroup == null) return original.call(instance, context);
        player.sendMessage(lockGroup.itemMessage(), true);
        return ActionResult.CONSUME;
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;")
    )
    private TypedActionResult<ItemStack> checkLock(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
        var lockGroup = LockGroup.findLocked(user, (ItemStack) (Object) this);
        if (lockGroup == null) return original.call(instance, world, user, hand);
        user.sendMessage(lockGroup.itemMessage(), true);
        return TypedActionResult.pass(user.getStackInHand(hand)); // shouldn't this be a fail?
    }

    @Inject(
            method = "getTooltip",
            at = @At("TAIL")
    )
    private void addTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> tooltip) {
        ItemLockTooltip.appendTooltip((ItemStack) (Object) this, player, tooltip);
    }

    @ModifyExpressionValue(
            method = "getTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;")
    )
    private Text modifyName(Text original, @Local(argsOnly = true) @Nullable PlayerEntity player) {
        if (player == null) return original;
        var lockGroup = LockGroup.findLocked(player, (ItemStack) (Object) this);
        if (lockGroup == null) return original;
        return lockGroup.itemNameText();
    }

    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "appendAttributeModifiersTooltip",
            at = @At("HEAD"),
            cancellable = true)
    private void hideModifiers(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo ci) {
        if (player != null && LockGroup.isLocked(player, (ItemStack) (Object) this))
            ci.cancel();
    }
}
