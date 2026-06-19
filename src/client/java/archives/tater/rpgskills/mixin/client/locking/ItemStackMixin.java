package archives.tater.rpgskills.mixin.client.locking;

import archives.tater.rpgskills.RequirementTooltip;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import org.jetbrains.annotations.Nullable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract boolean isOf(Item item);

    @WrapMethod(
            method = "getTooltip"
    )
    private List<Text> setTooltipStack(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, Operation<List<Text>> original) {
        RequirementTooltip.setBookTooltip(this.isOf(Items.ENCHANTED_BOOK));
        var result = original.call(context, player, type);
        RequirementTooltip.setBookTooltip(false);
        return result;
    }
}
