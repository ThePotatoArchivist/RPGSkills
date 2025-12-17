package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class LevelTitleWidget(x: Int, y: Int, width: Int, message: Text) :
    ClickableWidget(x, y, width, textRenderer.fontHeight + 2 * MARGIN, message) {

    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height)
        context.drawCenteredTextWithShadow(textRenderer, message, x + width / 2, y + MARGIN, 0xffffff)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    companion object {
        val BACKGROUND_TEXTURE = RPGSkills.id("border9")

        const val MARGIN = 8

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}