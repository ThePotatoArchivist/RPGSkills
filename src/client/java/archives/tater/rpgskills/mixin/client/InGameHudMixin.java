package archives.tater.rpgskills.mixin.client;

import archives.tater.rpgskills.data.LockCategories;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@WrapOperation(
			method = "renderHeldItemTooltip",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;")
	)
	private Text init(ItemStack instance, Operation<Text> original) {
		return LockCategories.lockProcessName(instance, MinecraftClient.getInstance().player, original.call(instance));
	}
}
