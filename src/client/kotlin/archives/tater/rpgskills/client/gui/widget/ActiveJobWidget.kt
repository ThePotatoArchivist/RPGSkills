package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.cca.JobsComponent
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.entity.ai.brain.task.TaskTriggerer.task
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.bettercombat.api.WeaponAttributesHelper.override

class ActiveJobWidget(private val job: RegistryEntry<Job>, private val jobsComponent: JobsComponent, width: Int, x: Int, y: Int) :
    ClickableWidget(x, y, width, (textRenderer.fontHeight + 1) * (job.value.tasks.size + 1) + 2 * MARGIN + 2, Text.empty()), AbstractJobWidget {

    val textWidth = width - MARGIN * 2

    val tasks = job.value.tasks.toList()

    init {
        height = getText().sumOf { textRenderer.getWrappedLinesHeight(it, textWidth) } + textRenderer.fontHeight + 2 + 2 * MARGIN
    }

    private fun getText(): List<Text> {
        val instance = jobsComponent[job] ?: return listOf()
        return tasks.map { (name, task) ->
            TASK.text(
                if (name in instance.tasks) INCOMPLETE_TASK.text else COMPLETE_TASK.text,
                TASK_PROGRESS.text(instance.tasks[name] ?: task.count, task.count).apply {
                    if (name in instance.tasks) withColor(0x5555FF)
                },
                Text.literal(task.description),
            ).apply {
                if (name !in instance.tasks) formatted(Formatting.DARK_GREEN)
            }
        }
    }

    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height)
        context.drawText(textRenderer, Text.literal(job.value.name), x + MARGIN, y + MARGIN, /*if (onCooldown) 0x909090 else*/ 0x404040, false)

        var currentY = y + textRenderer.fontHeight + 2 + MARGIN

        for (text in getText()) {
            context.drawTextWrapped(
                textRenderer,
                text,
                x + MARGIN,
                currentY,
                textWidth,
                0x404040,
            )
            currentY += textRenderer.getWrappedLinesHeight(text, textWidth)
        }

        drawReward(context, textRenderer, job, MARGIN)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    companion object {
        val BACKGROUND_TEXTURE = RPGSkills.id("border9")

        const val MARGIN = 6

        val TASK = Translation.arg("screen.widget.$MOD_ID.job.task")
        val INCOMPLETE_TASK = Translation.unit("screen.widget.$MOD_ID.job.incomplete_task")
        val COMPLETE_TASK = Translation.unit("screen.widget.$MOD_ID.job.complete_task")
        val TASK_PROGRESS = Translation.arg("screen.widget.$MOD_ID.job.task_progress")

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}
