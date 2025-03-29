package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @SuppressWarnings("ConstantValue")
    @WrapWithCondition(
            method = "getEquipmentChanges",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;addTemporaryModifiers(Lcom/google/common/collect/Multimap;)V")
    )
    private boolean checkLock(AttributeContainer instance, Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers, @Local(ordinal = 1) ItemStack stack2) {
        return !((Object) this instanceof PlayerEntity player) || !LockGroup.isLocked(player, stack2);
    }
}
