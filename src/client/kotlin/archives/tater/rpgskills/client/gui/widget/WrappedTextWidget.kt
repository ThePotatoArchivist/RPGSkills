package archives.tater.rpgskills.client.gui.widget

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.AbstractTextWidget
import net.minecraft.text.Text

class WrappedTextWidget(x: Int, y: Int, width: Int, private val margin: Int, message: Text, private val color: Int, textRenderer: TextRenderer) :
    AbstractTextWidget(x, y, width, 2 * margin + textRenderer.getWrappedLinesHeight(message, width - 2 * margin), message, textRenderer) {

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTextWrapped(textRenderer, message, x + margin, y + margin, width - 2 * margin, color)
    }
}