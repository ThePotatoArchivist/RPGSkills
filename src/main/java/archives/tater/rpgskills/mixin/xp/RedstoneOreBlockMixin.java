package archives.tater.rpgskills.mixin.xp;

import archives.tater.rpgskills.entity.SkillPointOrbEntity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;

@Mixin(RedstoneOreBlock.class)
public class RedstoneOreBlockMixin {
    @WrapOperation(
            method = "onStacksDropped",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneOreBlock;dropExperienceWhenMined(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/intprovider/IntProvider;)V")
    )
    private void dropSkillPoints(RedstoneOreBlock instance, ServerWorld world, BlockPos blockPos, ItemStack stack, IntProvider experience, Operation<Void> original, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) ItemStack tool) {
        SkillPointOrbEntity.spawnForBlock(world, pos, state, tool, experience.get(world.random));
        original.call(instance, world, blockPos, stack, experience);
    }
}
