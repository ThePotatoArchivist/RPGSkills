package archives.tater.rpgskills.mixin.xp;

import archives.tater.rpgskills.entity.SkillPointOrbEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {
    @Shadow
    public abstract @Nullable ServerPlayerEntity getLovingPlayer();

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;Lnet/minecraft/entity/passive/PassiveEntity;)V",
            at = @At("TAIL")
    )
    private void spawnSkillPoints(ServerWorld world, AnimalEntity other, PassiveEntity baby, CallbackInfo ci) {
        var owner = getLovingPlayer();
        if (owner != null && getWorld() instanceof ServerWorld serverWorld)
            SkillPointOrbEntity.spawnForBreeding(serverWorld, getPos(), owner);
    }
}
