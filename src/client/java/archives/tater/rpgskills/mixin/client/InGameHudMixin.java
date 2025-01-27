package archives.tater.rpgskills.mixin.client;

import archives.tater.rpgskills.data.LockCategories;
import archives.tater.rpgskills.data.SkillsComponent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow @Final private MinecraftClient client;

	@WrapOperation(
			method = "renderHeldItemTooltip",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;")
	)
	private Text init(ItemStack instance, Operation<Text> original) {
		return LockCategories.lockProcessName(instance, client.player, original.call(instance));
	}

	@SuppressWarnings("DataFlowIssue") // client.player is definitely not null here
    @ModifyVariable(
			method = "renderExperienceBar",
			at = @At("STORE")
	)
	private String modifyXpDisplay(String original) {
        return (SkillsComponent.KEY.get(client.player).getRemainingLevelPoints()) + "/" + original;
	}
}
