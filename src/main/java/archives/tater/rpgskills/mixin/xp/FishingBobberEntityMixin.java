package archives.tater.rpgskills.mixin.xp;

import archives.tater.rpgskills.entity.SkillPointOrbEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends Entity {
    @Shadow
    public abstract @Nullable PlayerEntity getPlayerOwner();

    public FishingBobberEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z", ordinal = 0)
    )
    private void spawnSkillPoints(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        var owner = getPlayerOwner();
        if (owner != null && getWorld() instanceof ServerWorld serverWorld)
            SkillPointOrbEntity.spawnForFishing(serverWorld, owner.getPos(), owner);
    }
}
