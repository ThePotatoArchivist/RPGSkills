package archives.tater.rpgskills.mixin.advancement.chipped;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import earth.terrarium.chipped.common.menus.WorkbenchMenu;

@Mixin(WorkbenchMenu.class)
public class WorkbenchMenuMixin {
    @Shadow
    @Final
    protected PlayerInventory inventory;

    @Shadow
    @Final
    protected World level;

    @ModifyExpressionValue(
            method = "craft",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copyWithCount(I)Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack triggerOnCrafted(ItemStack original) {
        original.onCraftByPlayer(level, inventory.player, original.getCount());
        return original;
    }
}
