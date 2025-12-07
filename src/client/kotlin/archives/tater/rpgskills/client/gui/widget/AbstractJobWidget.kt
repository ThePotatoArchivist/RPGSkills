package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.util.drawOutlinedText
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.Widget
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Formatting

interface AbstractJobWidget : Widget {
    fun drawReward(context: DrawContext, textRenderer: TextRenderer, job: RegistryEntry<Job>, rightX: Int, y: Int) {
        val rewardString = job.value.rewardPoints.toString()
        context.drawGuiTexture(
            ORB_ICON,
            this.x + rightX - textRenderer.getWidth(rewardString) - ORB_ICON_SIZE - 1,
            this.y + y,
            ORB_ICON_SIZE,
            ORB_ICON_SIZE
        )
        context.drawOutlinedText(
            textRenderer,
            rewardString,
            this.x + rightX - textRenderer.getWidth(rewardString),
            this.y + y,
            0x70DACD
        )
    }

    fun drawReward(context: DrawContext, textRenderer: TextRenderer, job: RegistryEntry<Job>, margin: Int) {
        drawReward(context, textRenderer, job, width - margin - 2, margin)
    }

    fun getTaskText(task: Job.Task) = TASK.text(TASK_COUNT.text(task.count), task.description)

    companion object {
        val ORB_ICON = RPGSkills.id("skill/skill_orb_small")

        const val ORB_ICON_SIZE = 9

        val TASK_COUNT = Translation.arg("screen.widget.rpgskills.job_unlock.task_count") {
            withColor(0x707070)
        }
        val TASK = Translation.arg("screen.widget.rpgskills.job_unlock.task")
    }
}
