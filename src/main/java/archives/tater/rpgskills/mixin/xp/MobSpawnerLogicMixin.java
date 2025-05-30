package archives.tater.rpgskills.mixin.xp;

import archives.tater.rpgskills.data.cca.DefeatSourceComponent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobSpawnerLogic.class)
public class MobSpawnerLogicMixin {
    @WrapOperation(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;playSpawnEffects()V")
    )
    private void setSpawnerSource(MobEntity instance, Operation<Void> original, @Local(argsOnly = true) BlockPos pos) {
        DefeatSourceComponent.onSpawnFromSpawner(instance, pos);
    }
}
