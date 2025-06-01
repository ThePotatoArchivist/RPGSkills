package archives.tater.rpgskills.mixin.client.attribute;

import archives.tater.rpgskills.RPGSkillsAttributes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelPredicateProviderRegistry.class)
public class ModelPredicateProviderRegistryMixin {
    @ModifyExpressionValue(
            method = "method_27890",
            at = @At(value = "CONSTANT", args = "floatValue=20.0")
    )
    private static float modifyDrawTime(float original, @Local(argsOnly = true) LivingEntity entity) {
        return (float) entity.getAttributeValue(RPGSkillsAttributes.BOW_DRAW_TIME);
    }
}
