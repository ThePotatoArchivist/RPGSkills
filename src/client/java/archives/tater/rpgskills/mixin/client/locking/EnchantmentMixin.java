package archives.tater.rpgskills.mixin.client.locking;

import archives.tater.rpgskills.data.LockGroup;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @ModifyExpressionValue(
            method = "getName",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;copy()Lnet/minecraft/text/MutableText;")
    )
    private static MutableText scrambleLocked(MutableText original, @Local(argsOnly = true) RegistryEntry<Enchantment> enchantment) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return original;
        if (!LockGroup.isLocked(player, enchantment)) return original;
        return original.setStyle(EnchantingPhrasesAccessor.getSTYLE());
    }
}
