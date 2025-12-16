package archives.tater.rpgskills.mixin.locking;

import archives.tater.rpgskills.data.LockGroup;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "appendAttributeModifiersTooltip",
            at = @At("HEAD"),
            cancellable = true)
    private void hideModifiers(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo ci) {
        if (player != null && LockGroup.isLocked(player, (ItemStack) (Object) this))
            ci.cancel();
    }
}
