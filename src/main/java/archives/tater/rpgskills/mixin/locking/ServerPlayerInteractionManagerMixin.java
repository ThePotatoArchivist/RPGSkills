package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.Nullable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @WrapOperation(
            method = "interactBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;")
    )
    private ItemActionResult lockBlock(BlockState instance, ItemStack stack, World world, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult, Operation<ItemActionResult> original, @Share("lockGroup") LocalRef<@Nullable Text> preventionMessage) {
        var lockGroup = LockGroup.findLocked(playerEntity, instance);
        if (lockGroup == null) return original.call(instance, stack, world, playerEntity, hand, blockHitResult);

        preventionMessage.set(lockGroup.blockMessage());
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @WrapOperation(
            method = "interactBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;")
    )
    private ActionResult lockBlock(BlockState instance, World world, PlayerEntity playerEntity, BlockHitResult blockHitResult, Operation<ActionResult> original, @Share("lockGroup") LocalRef<@Nullable Text> preventionMessage) {
        var lockGroup = LockGroup.findLocked(playerEntity, instance);
        if (lockGroup == null) return original.call(instance, world, playerEntity, blockHitResult);

        preventionMessage.set(lockGroup.blockMessage());
        return ActionResult.PASS;
    }

    @WrapOperation(
            method = "interactBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;")
    )
    private ActionResult lockItem(ItemStack instance, ItemUsageContext context, Operation<ActionResult> original, ServerPlayerEntity player, @Share("lockGroup") LocalRef<@Nullable Text> preventionMessage) {
        var lockGroup = LockGroup.findLocked(player, instance);
        if (lockGroup == null) return original.call(instance, context);

        preventionMessage.set(lockGroup.itemMessage());
        return ActionResult.PASS;
    }

    @ModifyReturnValue(
            method = "interactBlock",
            at = @At("TAIL")
    )
    private ActionResult showMessage(ActionResult original, ServerPlayerEntity player, @Share("lockGroup") LocalRef<@Nullable Text> preventionMessage) {
        if (original != ActionResult.PASS || preventionMessage.get() == null) return original;

        player.sendMessage(preventionMessage.get(), true);
        return ActionResult.FAIL;
    }
}
