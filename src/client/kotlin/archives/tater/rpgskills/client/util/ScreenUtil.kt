package archives.tater.rpgskills.client.util

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
