package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.client.util.drawOutlinedText
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class JobWidget(private val skillsComponent: SkillsComponent, private val job: RegistryEntry<Job>, width: Int, x: Int, y: Int) :
    ClickableWidget(x, y, width, (textRenderer.fontHeight + 1) * (job.value.tasks.size + 1) + 2 * MARGIN + 2, Text.empty()) {

    val tasks = job.value.tasks.toList()

    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val instance = skillsComponent[job] ?: run {
            context.drawText(textRenderer, Text.literal("ERROR Missing Job Instance"), x, y, 0xff0000, false)
            return
        }
        val onCooldown = instance.cooldown > 0

        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height)
        context.drawText(textRenderer, Text.literal(job.value.name), x + MARGIN, y + MARGIN, if (onCooldown) 0x909090 else 0x404040, false)

        tasks.forEachIndexed { i, (name, task) ->
            context.drawText(
                textRenderer,
                TASK.text(
                    if (name in instance.tasks) INCOMPLETE_TASK.text else COMPLETE_TASK.text,
                    Text.literal(task.description),
                    TASK_PROGRESS.text(instance.tasks[name] ?: task.count, task.count).apply {
                        if (!onCooldown && name in instance.tasks) withColor(0x5555FF)
                    },
                ).apply {
                    when {
                        onCooldown -> withColor(0x909090)
                        name !in instance.tasks -> formatted(Formatting.DARK_GREEN)
                    }
                },
                x + MARGIN,
                y + MARGIN + (i + 1) * (textRenderer.fontHeight + 1) + 2,
                0x404040,
                false
            )
        }
        if (onCooldown) {
            val timerString = "%d:%02d".format(instance.cooldown / 20 / 60, instance.cooldown / 20 % 60)
            context.drawText(
                textRenderer,
                timerString,
                x + width - MARGIN - textRenderer.getWidth(timerString),
                y + MARGIN,
                0x404040,
                false
            )
        } else {
            val rewardString = job.value.rewardPoints.toString()
            context.drawGuiTexture(
                ORB_ICON,
                x + width - MARGIN - textRenderer.getWidth(rewardString) - 2 - ORB_ICON_SIZE - 1,
                y + MARGIN,
                ORB_ICON_SIZE,
                ORB_ICON_SIZE
            )
            context.drawOutlinedText(
                textRenderer,
                rewardString,
                x + width - MARGIN - textRenderer.getWidth(rewardString) - 2,
                y + MARGIN,
                0x70DACD
            )
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    companion object {
        val BACKGROUND_TEXTURE = RPGSkills.id("border9")
        val ORB_ICON = RPGSkills.id("skill/skill_orb_small")

        const val ORB_ICON_SIZE = 9

        const val MARGIN = 6

        val TASK = Translation.arg("screen.widget.$MOD_ID.job.task")
        val INCOMPLETE_TASK = Translation.unit("screen.widget.$MOD_ID.job.incomplete_task")
        val COMPLETE_TASK = Translation.unit("screen.widget.$MOD_ID.job.complete_task")
        val TASK_PROGRESS = Translation.arg("screen.widget.$MOD_ID.job.task_progress")

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}