package archives.tater.rpgskills.mixin.xp;

import archives.tater.rpgskills.entity.SkillPointOrbEntity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;

@Mixin(ExperienceDroppingBlock.class)
public class ExperienceDroppingBlockMixin {
    @Shadow
    @Final
    private IntProvider experienceDropped;

    @Inject(
            method = "onStacksDropped",
            at = @At(value = "TAIL")
    )
    private void dropSkillPoints(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        SkillPointOrbEntity.spawnForBlock(world, pos, state, tool, experienceDropped.get(world.random));
    }
}
