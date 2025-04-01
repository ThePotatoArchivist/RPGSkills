package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @SuppressWarnings("ConstantValue")
    @WrapOperation(
            method = "getEquipmentChanges",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z")
    )
    private boolean checkLock(ItemStack instance, Operation<Boolean> original) {
        return original.call(instance) || ((Object) this instanceof PlayerEntity player) && LockGroup.isLocked(player, instance);
    }
}
