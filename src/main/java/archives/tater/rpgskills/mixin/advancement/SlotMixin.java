package archives.tater.rpgskills.mixin.advancement;

import archives.tater.rpgskills.criteria.ResultSlot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import org.jetbrains.annotations.NotNull;

@Mixin(Slot.class)
public class SlotMixin implements ResultSlot {
    @Unique
    private boolean isExternalResult = false;

    @Override
    public void rpgskills$setIsExternalResult() {
        isExternalResult = true;
    }

    @Inject(
            method = "onTakeItem",
            at = @At("HEAD")
    )
    private void triggerOnCrafted(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (isExternalResult)
            stack.onCraftByPlayer(player.getWorld(), player, stack.getCount());
    }
}
