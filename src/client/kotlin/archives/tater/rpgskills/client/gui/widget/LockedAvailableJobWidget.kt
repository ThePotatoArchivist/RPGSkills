package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder

class LockedAvailableJobWidget(x: Int, y: Int) : AbstractAvailableJobWidget(x, y) {
    override val isHighlighted: Boolean get() = false
    override val isDisabled: Boolean get() = true

    init {
        active = false
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        context.drawText(textRenderer, LOCKED.text, x + MARGIN, y + MARGIN + 1, 0x404040, false)
    }

    companion object {
        val LOCKED = Translation.unit("screen.widget.$MOD_ID.locked_job.locked")

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}