package archives.tater.rpgskills.mixin.client.compat.bettercombat;

import archives.tater.rpgskills.data.LockGroup;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.client.WeaponAttributeTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeaponAttributeTooltip.class)
public class WeaponAttributeTooltipMixin {
    @ModifyExpressionValue(
            method = "modifyTooltip",
            at = @At(value = "INVOKE", target = "Lnet/bettercombat/logic/WeaponRegistry;getAttributes(Lnet/minecraft/item/ItemStack;)Lnet/bettercombat/api/WeaponAttributes;")
    )
    private static WeaponAttributes disableTooltip(WeaponAttributes original, @Local(argsOnly = true) ItemStack stack) {
        var player = MinecraftClient.getInstance().player;
        return player != null && LockGroup.isLocked(player, stack) ? null : original;
    }
}
