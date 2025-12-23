package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.cca.JobsComponent
import archives.tater.rpgskills.client.util.getMousePosScrolled
import archives.tater.rpgskills.client.util.mouseIn
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.networking.AddJobPayload
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.component1
import archives.tater.rpgskills.util.component2
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class AvailableJobWidget(private val job: RegistryEntry<Job>, private val jobs: JobsComponent, x: Int, y: Int) :
    AbstractAvailableJobWidget(x, y) {

    private val canAdd get() = !jobs.isFull

    override val isHighlighted: Boolean get() = isHovered && canAdd
    override val isDisabled: Boolean get() = false

    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val (tMouseX, tMouseY) = getMousePosScrolled(context, mouseX, mouseY)
        hovered = context.scissorContains(mouseX, mouseY) && mouseIn(tMouseX, tMouseY, x, y, width, height)

        super.renderWidget(context, mouseX, mouseY, delta)
        context.drawText(textRenderer, job.value.name, x + MARGIN, y + MARGIN + 1, 0x404040, false)
        jobs.cooldowns[job]?.let { cooldown ->
            drawCooldown(context, textRenderer, cooldown, MARGIN)
        } ?:
            drawReward(context, textRenderer, job, MARGIN)

        if (isHovered) MinecraftClient.getInstance().currentScreen?.run {
            setTooltip(buildList {
                if (canAdd)
                    add(TOOLTIP_HINT.text.asOrderedText())
                for ((_, task) in job.value.tasks)
                    addAll(textRenderer.wrapLines(getTaskText(task), 180))
            })
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        hovered && super.mouseClicked(mouseX, mouseY, button)

    override fun clicked(mouseX: Double, mouseY: Double): Boolean = active && visible && hovered

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (canAdd)
            ClientPlayNetworking.send(AddJobPayload(job))
    }

    companion object {

        val TEXTURE = ButtonTextures(
            RPGSkills.id("skill/job_option"),
            RPGSkills.id("skill/job_option_highlighted"),
        )

        val TOOLTIP_HINT = Translation.unit("screen.widget.$MOD_ID.available_job.tooltip_hint") {
            formatted(Formatting.GRAY)
        }

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}