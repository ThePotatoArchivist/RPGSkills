package archives.tater.rpgskills.mixin.scaling;

import archives.tater.rpgskills.RPGSkillsTags;
import archives.tater.rpgskills.cca.BossTrackerComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float bossAssist(float amount, DamageSource source) {
        return source.getAttacker() != null && source.getAttacker().getType().isIn(RPGSkillsTags.BOSS_ATTRIBUTE_AFFECTED)
                ? BossTrackerComponent.modifyDamageTaken((PlayerEntity) (Object) this, amount)
                : amount;
    }
}
