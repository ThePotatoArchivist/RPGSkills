package archives.tater.rpgskills.mixin.advancement.freshcraft;

import archives.tater.rpgskills.cca.FreshCraftComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(
            method = "merge(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V")
    )
    private static void mergeFreshCount(ItemEntity targetEntity, ItemStack targetStack, ItemEntity sourceEntity, ItemStack sourceStack, CallbackInfo ci) {
        targetEntity.getComponent(FreshCraftComponent.KEY).addFreshCount(sourceEntity.getComponent(FreshCraftComponent.KEY).getFreshCount());
    }
}
