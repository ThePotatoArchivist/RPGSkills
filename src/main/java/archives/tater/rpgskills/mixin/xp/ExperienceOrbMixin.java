package archives.tater.rpgskills.mixin.xp;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperienceOrbEntity.class)
public interface ExperienceOrbMixin {
    @WrapWithCondition(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;expensiveUpdate()V")
    )
    default boolean rpgskills_shouldRunExpensiveUpdate(ExperienceOrbEntity instance) {
        return false;
    }
}
