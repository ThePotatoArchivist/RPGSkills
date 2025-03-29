@file:Environment(EnvType.CLIENT)

package archives.tater.rpgskills.client.util

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

fun button(
    message: Text,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    tooltip: Tooltip? = null,
    onPress: ButtonWidget.PressAction
): ButtonWidget = ButtonWidget.builder(message, onPress).apply {
    dimensions(x, y, width, height)
    tooltip(tooltip)
}.build()

fun button(
    message: Text,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    tooltip: Text,
    onPress: ButtonWidget.PressAction
): ButtonWidget = button(message, x, y, width, height, Tooltip.of(tooltip), onPress)

fun DrawContext.drawOutlinedText(
    textRenderer: TextRenderer,
    text: Text,
    x: Int,
    y: Int,
    textColor: Int = 0x80FF20,
    outlineColor: Int = 0x000000,
) {
    drawText(textRenderer, text, x + 1, y, outlineColor, false)
    drawText(textRenderer, text, x, y + 1, outlineColor, false)
    drawText(textRenderer, text, x + 1, y + 2, outlineColor, false)
    drawText(textRenderer, text, x + 2, y + 1, outlineColor, false)
    drawText(textRenderer, text, x + 1, y + 1, textColor, false)
}

fun DrawContext.drawOutlinedText(
    textRenderer: TextRenderer,
    text: String,
    x: Int,
    y: Int,
    textColor: Int = 0x80FF20,
    outlineColor: Int = 0x000000,
) {
    drawText(textRenderer, text, x + 1, y, outlineColor, false)
    drawText(textRenderer, text, x, y + 1, outlineColor, false)
    drawText(textRenderer, text, x + 1, y + 2, outlineColor, false)
    drawText(textRenderer, text, x + 2, y + 1, outlineColor, false)
    drawText(textRenderer, text, x + 1, y + 1, textColor, false)
}
