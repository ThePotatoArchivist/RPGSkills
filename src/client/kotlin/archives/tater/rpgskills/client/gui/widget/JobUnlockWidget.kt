package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class JobUnlockWidget(x: Int, y: Int, width: Int, private val job: RegistryEntry<Job>) :
    ClickableWidget(x, y, width, 0, Text.empty()), AbstractJobWidget {

    init {
        active = false
    }

    val text = buildList {
        val textWidth = width - 2 * MARGIN

        addAll(textRenderer.wrapLines(TITLE.text(job.value.name), textWidth))
        for ((_, task) in job.value.tasks)
            addAll(textRenderer.wrapLines(getTaskText(task), textWidth))
    }

    init {
        height = text.size * textRenderer.fontHeight + 2 * MARGIN
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        text.forEachIndexed { index, text ->
            context.drawText(textRenderer, text, x + MARGIN, y + MARGIN + index * textRenderer.fontHeight, 0x404040, false)
        }
        drawReward(context, textRenderer, job, MARGIN)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    companion object {
        val TITLE = Translation.arg("screen.widget.rpgskills.job_unlock.title") {
            formatted(Formatting.BLACK)
        }

        const val MARGIN = 6

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }

}
