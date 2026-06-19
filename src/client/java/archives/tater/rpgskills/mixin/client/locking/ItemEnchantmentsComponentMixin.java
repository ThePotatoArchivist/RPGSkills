package archives.tater.rpgskills.mixin.client.locking;

import archives.tater.rpgskills.RPGSkills;
import archives.tater.rpgskills.RPGSkillsClient;
import archives.tater.rpgskills.RequirementTooltip;
import archives.tater.rpgskills.data.LockGroup;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.function.Consumer;

@Mixin(ItemEnchantmentsComponent.class)
public class ItemEnchantmentsComponentMixin {
    @WrapOperation(
            method = "appendTooltip",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V")
    )
    private void addTooltip(Consumer<Text> instance, Object t, Operation<Object> original, @Local RegistryEntry<Enchantment> enchantment) {
        original.call(instance, t);

        if (!RequirementTooltip.isBookTooltip()) return;

        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        RequirementTooltip.appendTooltip(enchantment, player, instance, RPGSkillsClient.requirementsKey.isDown(), Text.keybind(RPGSkillsClient.requirementsKey.getTranslationKey()));
    }
}
