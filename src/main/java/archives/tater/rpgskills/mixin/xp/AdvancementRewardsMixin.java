package archives.tater.rpgskills.mixin.xp;

import archives.tater.rpgskills.entity.SkillPointOrbEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.server.network.ServerPlayerEntity;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

@Mixin(AdvancementRewards.class)
public class AdvancementRewardsMixin {
    @Inject(
            method = "apply",
            at = @At("HEAD")
    )
    private void spawnSkillPoints(ServerPlayerEntity player, CallbackInfo ci) {
        SkillPointOrbEntity.spawnForAdvancement(player.getServerWorld(), (AdvancementRewards) (Object) this, player);
    }
}
