package archives.tater.rpgskills.mixin.advancement.brewinandchewin;

import archives.tater.rpgskills.criteria.ResultSlot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;

import umpaz.brewinandchewin.common.block.entity.container.KegMenu;

@Mixin(KegMenu.class)
public class KegMenuMixin {
    @ModifyExpressionValue(
            method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lumpaz/brewinandchewin/common/block/entity/KegBlockEntity;Lnet/minecraft/screen/PropertyDelegate;)V",
            at = @At(value = "INVOKE", target = "Lumpaz/brewinandchewin/platform/BnCPlatformHelper;createKegResultSlot(Lumpaz/brewinandchewin/common/container/AbstractedItemHandler;III)Lnet/minecraft/screen/slot/Slot;")
    )
    private Slot setSlot(Slot original, @Local(argsOnly = true) PlayerInventory playerInventory) {
        ((ResultSlot) original).rpgskills$setIsExternalResult();
        return original;
    }
}
