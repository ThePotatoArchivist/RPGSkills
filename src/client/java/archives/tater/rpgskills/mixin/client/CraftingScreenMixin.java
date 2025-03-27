package archives.tater.rpgskills.mixin.client;

import archives.tater.rpgskills.RPGSkills;
import archives.tater.rpgskills.RPGSkillsClient;
import archives.tater.rpgskills.data.LockGroup;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends HandledScreen<CraftingScreenHandler> {
    @Unique
    private static final Identifier CROSSED_ARROW_TEXTURE = RPGSkills.id("textures/gui/container/crossed_arrow.png");
    @Unique
    private static final int CROSSED_ARROW_WIDTH = 28;
    @Unique
    private static final int CROSSED_ARROW_HEIGHT = 21;

    public CraftingScreenMixin(CraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void renderCross(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (RPGSkillsClient.blockedRecipeGroup != null) {
            var crossX = x + 87;
            var crossY = (height - backgroundHeight) / 2 + 32;
            context.drawTexture(CROSSED_ARROW_TEXTURE, crossX, crossY, 0, 0, CROSSED_ARROW_WIDTH, CROSSED_ARROW_HEIGHT, 32, 32);
            if (this.handler.getCursorStack().isEmpty() && (this.focusedSlot == null || !this.focusedSlot.hasStack())
                    && mouseX > crossX && mouseX < crossX + CROSSED_ARROW_WIDTH
                    && mouseY > crossY && mouseY < crossY + CROSSED_ARROW_HEIGHT
            )
                context.drawTooltip(this.textRenderer, LockGroup.recipeMessage(RPGSkillsClient.blockedRecipeGroup), mouseX, mouseY);
        }
    }
}
