package archives.tater.rpgskills.mixin.client.xp;

import archives.tater.rpgskills.client.render.SkillBarRenderer;
import archives.tater.rpgskills.data.LockGroup;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Shadow @Final private MinecraftClient client;

	@Shadow public abstract TextRenderer getTextRenderer();

	@Shadow protected abstract boolean shouldRenderExperience();

	@SuppressWarnings("DataFlowIssue") // client.player is definitely not null here
	@WrapOperation(
			method = "renderHeldItemTooltip",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;")
	)
	private Text init(ItemStack instance, Operation<Text> original) {
        var lockGroup = LockGroup.findLocked(client.player, instance);
		return lockGroup == null ? original.call(instance) : lockGroup.itemNameText();
	}

	@SuppressWarnings("DataFlowIssue") // client.player is definitely not null here
	@Inject(
			method = "renderExperienceBar",
			at = @At("HEAD"),
			cancellable = true
	)
	private void replaceBar(DrawContext context, int x, CallbackInfo ci) {
		if (!SkillBarRenderer.shouldRender()) return;
		SkillBarRenderer.renderBar(context, x, client.player);
		ci.cancel();
	}

	@SuppressWarnings("DataFlowIssue") // client.player is definitely not null here
    @Inject(
			method = "renderExperienceLevel",
			at = @At("HEAD"),
			cancellable = true
	)
	private void modifyXpDisplay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if (!SkillBarRenderer.shouldRender() || !shouldRenderExperience()) return;
		SkillBarRenderer.renderLevel(context, getTextRenderer(), client.player);
		ci.cancel();
	}
}
