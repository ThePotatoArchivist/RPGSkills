package archives.tater.rpgskills.mixin.job;

import archives.tater.rpgskills.data.cca.RPGSkillsComponents;
import archives.tater.rpgskills.data.cca.SkillsComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Predicate;

@Mixin(AbstractCriterion.class)
public abstract class AbstractCriterionMixin<T extends AbstractCriterion.Conditions> implements Criterion<T> {
    @Inject(
            method = "trigger",
            at = @At("TAIL")
    )
    private void triggerJobs(ServerPlayerEntity player, Predicate<T> predicate, CallbackInfo ci) {
        SkillsComponent.KEY.get(player).onCriterion(this, predicate);
    }
}
