package archives.tater.rpgskills.mixin.advancement;

import archives.tater.rpgskills.RPGSkillsStats;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import static java.lang.Math.exp;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow
    public abstract void increaseStat(Identifier stat, int amount);

    @Inject(
            method = "addExperience",
            at = @At("HEAD")
    )
    private void xpStat(int experience, CallbackInfo ci) {
        increaseStat(RPGSkillsStats.XP_POINTS_COLLECTED, experience);
    }
}
