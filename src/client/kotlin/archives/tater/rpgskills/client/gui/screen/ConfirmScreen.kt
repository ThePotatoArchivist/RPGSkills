package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.util.Translation
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

class ConfirmScreen(private val message: List<Text>, private val parent: Screen, private val onConfirm: () -> Unit) : Screen(Text.empty()) {
    override fun init() {
        addDrawableChild(ButtonWidget.builder(OK.text) { onConfirm() }.apply {
            position(width / 3 - ButtonWidget.DEFAULT_WIDTH / 2, 2 * height / 3 - ButtonWidget.DEFAULT_HEIGHT / 2)
        }.build())
        addDrawableChild(ButtonWidget.builder(CANCEL.text) {
            client?.setScreen(parent)
        }.apply {
            position(2 * width / 3 - ButtonWidget.DEFAULT_WIDTH / 2, 2 * height / 3 - ButtonWidget.DEFAULT_HEIGHT / 2)
        }.build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        message.forEachIndexed { index, line ->
            context.drawCenteredTextWithShadow(textRenderer, line, width / 2, height / 3 + ((2 * index - (message.size - 1)) * textRenderer.fontHeight), 0xffffff)
        }
    }

    companion object {
        val OK = Translation.unit("screen.$MOD_ID.confirm.ok")
        val CANCEL = Translation.unit("screen.$MOD_ID.confirm.cancel")
    }
}