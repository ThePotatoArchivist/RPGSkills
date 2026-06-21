package archives.tater.rpgskills.mixin.advancement.freshcraft;

import archives.tater.rpgskills.cca.FreshCraftComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getStack();

    @Inject(
            method = "tryMerge(Lnet/minecraft/entity/ItemEntity;)V",
            at = @At("TAIL")
    )
    private void mergeFreshCount(ItemEntity other, CallbackInfo ci) {
        Entity source;
        Entity target;
        if (getStack().isEmpty()) {
            source = this;
            target = other;
        } else if (other.getStack().isEmpty()) {
            source = other;
            target= this;
        } else return;

        target.getComponent(FreshCraftComponent.KEY).addFreshCount(source.getComponent(FreshCraftComponent.KEY).getFreshCount());
    }
}
