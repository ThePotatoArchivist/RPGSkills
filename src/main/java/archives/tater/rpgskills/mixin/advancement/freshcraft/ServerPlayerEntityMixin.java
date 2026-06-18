package archives.tater.rpgskills.mixin.advancement.freshcraft;

import archives.tater.rpgskills.cca.FreshCraftComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(
            method = "triggerItemPickedUpByEntityCriteria",
            at = @At("TAIL")
    )
    private void triggerCraft(ItemEntity item, CallbackInfo ci) {
        var freshCount = item.getComponent(FreshCraftComponent.KEY).getFreshCount();
        if (freshCount > 0)
            item.getStack().onCraftByPlayer(getWorld(), this, freshCount);
    }
}
