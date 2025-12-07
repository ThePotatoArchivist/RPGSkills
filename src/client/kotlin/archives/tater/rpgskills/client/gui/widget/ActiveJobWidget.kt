package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.RPGSkillsClient
import archives.tater.rpgskills.RPGSkillsConfig
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.cca.JobsComponent
import archives.tater.rpgskills.client.util.getMousePosScrolled
import archives.tater.rpgskills.client.util.mouseIn
import archives.tater.rpgskills.networking.AddJobPayload
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
import net.minecraft.command.argument.EntityArgumentType.player
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ActiveJobWidget(private val job: RegistryEntry<Job>, player: PlayerEntity, width: Int, x: Int, y: Int) :
    ClickableWidget(x, y, width, (textRenderer.fontHeight + 1) * (job.value.tasks.size + 1) + 2 * MARGIN + 2, Text.empty()), AbstractJobWidget {

    private val jobsComponent = player[JobsComponent]
    private val registryManager = player.registryManager

    private val textWidth = width - MARGIN * 2 - CHECKBOX_WIDTH

    private var closeHovered = false

    init {
        height = getTaskText().sumOf { (text, _) -> textRenderer.getWrappedLinesHeight(text, textWidth) + 2 } + textRenderer.fontHeight + 4 + 16 + 2 * MARGIN
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

    fun isCloseHovered(context: DrawContext, mouseX: Int, mouseY: Int): Boolean {
        val (tMouseX, tMouseY) = getMousePosScrolled(context, mouseX, mouseY)
        return mouseIn(tMouseX, tMouseY, x + width - MARGIN, y + height - MARGIN, -CLOSE_SIZE, -CLOSE_SIZE)
    }

    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height)
        context.drawText(textRenderer, Text.literal(job.value.name), x + MARGIN, y + MARGIN, /*if (onCooldown) 0x909090 else*/ 0x404040, false)

        var currentY = y + textRenderer.fontHeight + 4 + MARGIN

        for ((text, pending) in getTaskText()) {
            context.drawText(textRenderer, if (pending) INCOMPLETE_TASK.text else COMPLETE_TASK.text, x + MARGIN, currentY, if (pending) 0x404040 else 0x00aa00, false)
            context.drawTextWrapped(
                textRenderer,
                text,
                x + MARGIN + CHECKBOX_WIDTH,
                currentY,
                textWidth,
                0x404040,
            )
            currentY += textRenderer.getWrappedLinesHeight(text, textWidth) + 2
        }

        context.drawItemWithoutEntity(RPGSkillsClient.JOB_SKILL_CACHE[registryManager][job]?.value?.icon ?: ItemStack.EMPTY, x + MARGIN, currentY)

        closeHovered = isCloseHovered(context, mouseX, mouseY)
        context.drawGuiTexture(CLOSE_TEXTURE[true, closeHovered], x + width - MARGIN - CLOSE_SIZE, y + height - MARGIN - CLOSE_SIZE, CLOSE_SIZE, CLOSE_SIZE)

        drawReward(context, textRenderer, job, MARGIN)
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
        const val CHECKBOX_WIDTH = 13
        const val CLOSE_SIZE = 11

        val INCOMPLETE_TASK = Translation.unit("screen.widget.$MOD_ID.job.incomplete_task")
        val COMPLETE_TASK = Translation.unit("screen.widget.$MOD_ID.job.complete_task")
        val TASK_PROGRESS = Translation.arg("screen.widget.$MOD_ID.job.task_progress")

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}
