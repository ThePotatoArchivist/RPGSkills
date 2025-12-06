package archives.tater.rpgskills.mixin.xp;

import archives.tater.rpgskills.cca.DefeatSourceComponent;
import archives.tater.rpgskills.entity.SkillPointOrbEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private void afterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DefeatSourceComponent.afterDamage((LivingEntity) (Object) this, source, amount);
    }
}
