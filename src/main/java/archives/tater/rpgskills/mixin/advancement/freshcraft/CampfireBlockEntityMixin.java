package archives.tater.rpgskills.mixin.advancement.freshcraft;

import archives.tater.rpgskills.cca.FreshCraftComponent;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {
    @WrapOperation(
            method = "litServerTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V")
    )
    private static void setScope(World world, double x, double y, double z, ItemStack stack, Operation<Void> original) {
        FreshCraftComponent.IS_FRESH_CRAFT.set(true);
        original.call(world, x, y, z, stack);
        FreshCraftComponent.IS_FRESH_CRAFT.set(false);
    }
}
