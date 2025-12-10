package archives.tater.rpgskills.mixin.advancement;

import archives.tater.rpgskills.criteria.ItemCraftedCriterion;
import archives.tater.rpgskills.criteria.RPGSkillsCriteria;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
            method = "onCraftByPlayer",
            at = @At("TAIL")
    )
    private void triggerItemCrafted(World world, PlayerEntity player, int amount, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer)
            RPGSkillsCriteria.CRAFT_ITEM.trigger(serverPlayer, (ItemStack) (Object) this, amount);
    }
}
