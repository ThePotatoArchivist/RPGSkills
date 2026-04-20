package archives.tater.rpgskills.mixin.compat.penchant;

import archives.tater.penchant.menu.PenchantmentMenu;
import archives.tater.rpgskills.data.LockGroup;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

import net.spell_engine.rpg_series.loot.LootConfig;

import java.util.stream.Stream;

@Mixin(PenchantmentMenu.class)
public class PenchantmentMenuMixin {
    @Shadow
    @Final
    private PlayerEntity player;

    @ModifyExpressionValue(
            method = "setUnlockedEnchantments",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;concat(Ljava/util/stream/Stream;Ljava/util/stream/Stream;)Ljava/util/stream/Stream;")
    )
    private Stream<RegistryEntry<Enchantment>> allowedEnchantments(Stream<RegistryEntry<Enchantment>> original) {
        return original.filter(enchantment -> !LockGroup.isLocked(player, enchantment));
    }
}
