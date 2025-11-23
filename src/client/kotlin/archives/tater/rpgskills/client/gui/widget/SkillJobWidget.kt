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

class SkillJobWidget(x: Int, y: Int, width: Int, job: RegistryEntry<Job>) :
    ClickableWidget(x, y, width, textRenderer.fontHeight * 2 + 2 * MARGIN, Text.empty()) {

    val text = listOf(
        TITLE.text(job.value.name),
        Text.literal(job.value.description).withColor(0x707070),
    )

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height)
        text.forEachIndexed { index, text ->
            context.drawText(textRenderer, text, x + MARGIN, y + MARGIN + index * textRenderer.fontHeight, 0x404040, false)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    companion object {
        val TITLE = Translation.arg("screen.widget.rpgskills.jobs.title")

        val BACKGROUND_TEXTURE = RPGSkills.id("border9")

        const val MARGIN = 6

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }

}
