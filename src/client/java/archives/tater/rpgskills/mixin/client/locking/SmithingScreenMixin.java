package archives.tater.rpgskills.mixin.client.locking;

import archives.tater.rpgskills.client.render.CrossedArrowRenderer;
import archives.tater.rpgskills.data.LockGroup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

@Mixin(SmithingScreen.class)
public class SmithingScreenMixin {
    @ModifyArg(
            method = "method_48475",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawOrderedTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V"),
            index = 1
    )
    private List<? extends OrderedText> modifyError(List<? extends OrderedText> text) {
        var message = CrossedArrowRenderer.getBlockMessage(LockGroup::recipeMessage);
        if (message == null) {
            return text;
        }
        return message.stream().map(Text::asOrderedText).toList();
    }
}
