package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.RPGSkillsClient
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.cca.JobsComponent
import archives.tater.rpgskills.client.util.getMousePosScrolled
import archives.tater.rpgskills.client.util.mouseIn
import archives.tater.rpgskills.networking.RemoveJobPayload
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.component1
import archives.tater.rpgskills.util.component2
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ActiveJobWidget(private val job: RegistryEntry<Job>, player: PlayerEntity, width: Int, x: Int, y: Int) :
    ClickableWidget(x, y, width, (textRenderer.fontHeight + 1) * (job.value.tasks.size + 1) + 2 * MARGIN + 2, Text.empty()), AbstractJobWidget {

    private val jobsComponent = player[JobsComponent]
    private val registryManager = player.registryManager

    private val textWidth = width - MARGIN * 2 - CHECKBOX_WIDTH - TASK_MARGIN * 2

    private var closeHovered = false

    init {
        height = getTaskText().sumOf { (text, _) -> textRenderer.getWrappedLinesHeight(text, textWidth) + TASK_MARGIN } + textRenderer.fontHeight + TASK_MARGIN + 16 + 2 * MARGIN
    }

    private fun getTaskText(): List<Pair<Text, Boolean>> {
        val instance = jobsComponent[job] ?: return listOf()
        return job.value.tasks.map { (name, task) ->
            val pending = name in instance.tasks

            Text.empty().apply {
                append(TASK_PROGRESS.text(instance.tasks[name] ?: task.count, task.count).apply {
                    if (pending)
                        withColor(0x5555FF)
                })
                append(" ")
                append(Text.literal(task.description))
                if (!pending)
                    formatted(Formatting.DARK_GREEN)
            } to pending
        }
    }

    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val (tMouseX, tMouseY) = getMousePosScrolled(context, mouseX, mouseY)

        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height)
        context.drawText(textRenderer, Text.literal(job.value.name), x + MARGIN, y + MARGIN, /*if (onCooldown) 0x909090 else*/ 0x404040, false)

        var currentY = y + textRenderer.fontHeight + TASK_MARGIN + MARGIN

        for ((text, pending) in getTaskText()) {
            context.drawText(textRenderer, if (pending) INCOMPLETE_TASK.text else COMPLETE_TASK.text, x + MARGIN + TASK_MARGIN, currentY, if (pending) 0x404040 else 0x00aa00, false)
            context.drawTextWrapped(
                textRenderer,
                text,
                x + MARGIN + TASK_MARGIN + CHECKBOX_WIDTH,
                currentY,
                textWidth,
                0x404040,
            )
            currentY += textRenderer.getWrappedLinesHeight(text, textWidth) + TASK_MARGIN
        }

        RPGSkillsClient.JOB_SKILL_CACHE[registryManager][job]?.let { skill ->
            context.drawItemWithoutEntity(skill.value.icon, x + MARGIN, currentY)
            if (mouseIn(tMouseX, tMouseY, x + MARGIN, currentY, 16, 16))
                MinecraftClient.getInstance().currentScreen?.setTooltip(Text.literal(skill.value.name))
        }

        closeHovered = mouseIn(tMouseX, tMouseY, this.x + this.width - MARGIN, this.y + MARGIN, -CLOSE_SIZE, CLOSE_SIZE)
        context.drawGuiTexture(CLOSE_TEXTURE[true, closeHovered], x + width - MARGIN - CLOSE_SIZE, y + MARGIN, CLOSE_SIZE, CLOSE_SIZE)
        if (closeHovered)
            MinecraftClient.getInstance().currentScreen?.setTooltip(CANCEL.text)

        drawReward(context, textRenderer, job, width - MARGIN, height - MARGIN - textRenderer.fontHeight)

    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        closeHovered && super.mouseClicked(mouseX, mouseY, button)

    override fun clicked(mouseX: Double, mouseY: Double): Boolean =
        active && visible && closeHovered

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (closeHovered)
            ClientPlayNetworking.send(RemoveJobPayload(job))
    }

    companion object {
        val BACKGROUND_TEXTURE = RPGSkills.id("border9")
        val CLOSE_TEXTURE = ButtonTextures(
            RPGSkills.id("skill/close"),
            RPGSkills.id("skill/close_highlighted")
        )

        const val MARGIN = 6
        const val TASK_MARGIN = 6
        const val CHECKBOX_WIDTH = 13
        const val CLOSE_SIZE = 11

        val INCOMPLETE_TASK = Translation.unit("screen.widget.$MOD_ID.job.incomplete_task")
        val COMPLETE_TASK = Translation.unit("screen.widget.$MOD_ID.job.complete_task")
        val TASK_PROGRESS = Translation.arg("screen.widget.$MOD_ID.job.task_progress")
        val CANCEL = Translation.unit("screen.widget.$MOD_ID.job.cancel")

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}
