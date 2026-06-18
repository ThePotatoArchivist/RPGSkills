package archives.tater.rpgskills.mixin.advancement.freshcraft;

import archives.tater.rpgskills.cca.FreshCraftComponent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;

@Mixin(ItemScatterer.class)
public class ItemScattererMixin {
    @ModifyExpressionValue(
            method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V",
            at = @At(value = "NEW", target = "(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/ItemEntity;")
    )
    private static ItemEntity setFresh(ItemEntity original) {
        if (FreshCraftComponent.IS_FRESH_CRAFT.get())
            original.getComponent(FreshCraftComponent.KEY).setFreshCount(original.getStack().getCount());

        return original;
    }
}
