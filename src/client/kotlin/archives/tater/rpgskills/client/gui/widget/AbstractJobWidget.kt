package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.util.drawOutlinedText
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.value
import net.minecraft.advancement.AdvancementFrame
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.Widget
import net.minecraft.entity.ai.brain.task.SonicBoomTask.cooldown
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

    fun drawCooldown(context: DrawContext, textRenderer: TextRenderer, cooldown: Int, rightX: Int, y: Int) {
        val seconds = cooldown ceilDiv 20
        val timerString = "%d:%02d".format(seconds / 60, seconds % 60)
        context.drawText(
            textRenderer,
            timerString,
            this.x + rightX - textRenderer.getWidth(timerString),
            this.y + y,
            0x404040,
            false
        )
    }

    fun drawCooldown(context: DrawContext, textRenderer: TextRenderer, cooldown: Int, margin: Int) {
        drawCooldown(context, textRenderer, cooldown, width - margin, margin + 1)
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
