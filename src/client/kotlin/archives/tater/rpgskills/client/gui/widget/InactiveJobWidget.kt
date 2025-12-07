package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import com.ibm.icu.impl.ValidIdentifiers
import net.bettercombat.api.WeaponAttributesHelper.override

class InactiveJobWidget(private val job: RegistryEntry<Job>, x: Int, y: Int) : ClickableWidget(
    x,
    y,
    WIDTH,
    HEIGHT,
    Text.empty(),
), AbstractJobWidget {
    val tooltip = job.value.tasks.map { (_, task) -> getTaskText(task).asOrderedText() }

    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        context.drawGuiTexture(TEXTURE[true, isHovered], x, y, width, height)
        context.drawText(textRenderer, job.value.name, x + MARGIN, y + MARGIN, 0xffffff, true)
        drawReward(context, textRenderer, job, MARGIN)
        if (isHovered) MinecraftClient.getInstance().currentScreen?.run {
            setTooltip(tooltip)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    companion object {
        const val WIDTH = 128
        const val HEIGHT = 17
        const val MARGIN = 4

        val TEXTURE = ButtonTextures(
            RPGSkills.id("skill/job_option"),
            RPGSkills.id("skill/job_option_highlighted"),
        )

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}