package archives.tater.rpgskills.mixin.xp;

import archives.tater.rpgskills.data.cca.DefeatSourceComponent;
import archives.tater.rpgskills.entity.SkillPointOrbEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(
            method = "sendPickup",
            constant = @Constant(classValue = ExperienceOrbEntity.class)
    )
    private boolean allowSkillOrbPickup(Object object, Operation<Boolean> original) {
        return original.call(object) || object instanceof SkillPointOrbEntity;
    }

    @Inject(
            method = "damage",
            at = @At("TAIL")
    )
    private void afterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) float dealt, @Local(ordinal = 0) boolean blocked) {
        DefeatSourceComponent.afterDamage((LivingEntity) (Object) this, source, dealt, amount, blocked);
    }
}
