package archives.tater.rpgskills.mixin.client.locking;

import archives.tater.rpgskills.client.render.CrossedArrowRenderer;
import archives.tater.rpgskills.data.LockGroup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

@Mixin(ForgingScreen.class)
public abstract class ForgingScreenHandler<T extends ScreenHandler> extends HandledScreen<T> {
    public ForgingScreenHandler(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void renderCrossTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CrossedArrowRenderer.renderTooltip(
                context,
                this,
                x + 99,
                y + 45,
                28,
                21,
                mouseX,
                mouseY,
                LockGroup::enchantmentMessage
        );
    }
}
