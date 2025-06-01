package archives.tater.rpgskills.mixin.attribute;

import archives.tater.rpgskills.RPGSkillsAttributes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Unique
    private static @Nullable LivingEntity rpgskills$user = null;

    @WrapOperation(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F")
    )
    private float storeUser(int useTicks, Operation<Float> original, @Local(argsOnly = true) LivingEntity user) {
        rpgskills$user = user;
        var progress = original.call(useTicks);
        rpgskills$user = null;
        return progress;
    }

    @ModifyExpressionValue(
            method = "getPullProgress",
            at = @At(value = "CONSTANT", args = "floatValue=20.0")
    )
    private static float modifyDrawTime(float original) {
        return rpgskills$user == null ? original : (float) rpgskills$user.getAttributeInstance(RPGSkillsAttributes.BOW_DRAW_TIME).getValue();
    }

    @ModifyArg(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;shootAll(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/entity/LivingEntity;)V"),
            index = 6
    )
    private float modifyDivergence(float par6, @Local(argsOnly = true) LivingEntity user) {
        return (float) user.getAttributeValue(RPGSkillsAttributes.PROJECTILE_DIVERGENCE);
    }
}
