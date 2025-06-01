package archives.tater.rpgskills.mixin.attribute;

import archives.tater.rpgskills.RPGSkillsAttributes;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @ModifyArg(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;shootAll(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;FFLnet/minecraft/entity/LivingEntity;)V"),
            index = 5
    )
    private float modifyDivergence(float par6, @Local(argsOnly = true) PlayerEntity user) {
        return (float) user.getAttributeValue(RPGSkillsAttributes.PROJECTILE_DIVERGENCE);
    }
}
