package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.cca.JobsComponent
import archives.tater.rpgskills.client.util.getMousePosScrolled
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
    ClickableWidget(x, y, WIDTH, HEIGHT, Text.empty()), AbstractJobWidget {

    private val canAdd get() = !jobs.isFull

    override fun renderWidget(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val (tMouseX, tMouseY) = getMousePosScrolled(context, mouseX, mouseY)
        hovered = context.scissorContains(mouseX, mouseY) && tMouseX in x..<(x + width) && tMouseY in y..<(y + height)

        context.drawGuiTexture(TEXTURE[true, isHovered && canAdd], x, y, width, height)
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

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        hovered && super.mouseClicked(mouseX, mouseY, button)

    override fun clicked(mouseX: Double, mouseY: Double): Boolean = active && visible && hovered

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (canAdd)
            ClientPlayNetworking.send(AddJobPayload(job))
    }

    companion object {
        const val WIDTH = 128
        const val HEIGHT = 18
        const val MARGIN = 4

        val TEXTURE = ButtonTextures(
            RPGSkills.id("skill/job_option"),
            RPGSkills.id("skill/job_option_highlighted"),
        )

        val TOOLTIP_HINT = Translation.unit("screen.widget.rpgskills.available_job.tooltip_hint") {
            formatted(Formatting.GRAY)
        }

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}