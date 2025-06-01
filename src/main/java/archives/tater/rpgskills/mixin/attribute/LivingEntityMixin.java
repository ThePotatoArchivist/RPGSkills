package archives.tater.rpgskills.mixin.attribute;

import archives.tater.rpgskills.RPGSkillsAttributes;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyReturnValue(
            method = "createLivingAttributes",
            at = @At("RETURN")
    )
    private static DefaultAttributeContainer.Builder addAttributes(DefaultAttributeContainer.Builder original) {
        return original
                .add(RPGSkillsAttributes.BOW_DRAW_TIME)
                .add(RPGSkillsAttributes.PROJECTILE_DIVERGENCE);
    }
}
