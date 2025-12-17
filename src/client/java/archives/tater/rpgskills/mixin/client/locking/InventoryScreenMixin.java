package archives.tater.rpgskills.mixin.client.locking;

import archives.tater.rpgskills.client.render.CrossedArrowRenderer;
import archives.tater.rpgskills.data.LockGroup;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    @Unique
    private PlayerEntity player;

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void savePlayer(PlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void renderCross(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CrossedArrowRenderer.render(context,
                this,
                x + 133,
                y + 27,
                mouseX,
                mouseY,
                LockGroup::recipeMessage,
                player,
                true);
    }
}
