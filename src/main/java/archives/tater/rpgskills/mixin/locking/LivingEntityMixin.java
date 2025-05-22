package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow protected abstract void onRemoval(Entity.RemovalReason reason);

    @SuppressWarnings("ConstantValue")
    @WrapOperation(
            method = "getEquipmentChanges",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z")
    )
    private boolean checkLock(ItemStack instance, Operation<Boolean> original) {
        return original.call(instance) || ((Object) this instanceof PlayerEntity player) && LockGroup.isLocked(player, instance);
    }

    @SuppressWarnings("ConstantValue")
    @WrapOperation(
            method = "getPreferredEquipmentSlot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Equipment;fromStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/Equipment;")
    )
    private Equipment preventEquipLocked(ItemStack stack, Operation<Equipment> original) {
        return ((Object) this instanceof PlayerEntity player) && LockGroup.isLocked(player, stack) ? null : original.call(stack);
    }
}
