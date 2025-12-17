package archives.tater.rpgskills.mixin.client.locking;

import archives.tater.rpgskills.client.render.CrossedArrowRenderer;
import archives.tater.rpgskills.data.LockGroup;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends HandledScreen<CraftingScreenHandler> {
    @Unique
    private PlayerEntity player;

    public CraftingScreenMixin(CraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void savePlayer(CraftingScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        player = inventory.player;
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void renderCross(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        CrossedArrowRenderer.render(context,
                this,
                x + 87,
                (height - backgroundHeight) / 2 + 32,
                mouseX,
                mouseY,
                LockGroup::recipeMessage,
                player,
                false
        );
    }
}
