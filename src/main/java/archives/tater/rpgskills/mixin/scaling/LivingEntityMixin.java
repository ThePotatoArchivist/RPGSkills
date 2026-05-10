package archives.tater.rpgskills.mixin.scaling;

import archives.tater.rpgskills.RPGSkillsTags;
import archives.tater.rpgskills.cca.BossTrackerComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float bossAssist(float amount, DamageSource source) {
        return source.getAttacker() instanceof PlayerEntity player && getType().isIn(RPGSkillsTags.BOSS_ATTRIBUTE_AFFECTED)
                ? BossTrackerComponent.modifyDamageDealt(player, amount)
                : amount;
    }
}
