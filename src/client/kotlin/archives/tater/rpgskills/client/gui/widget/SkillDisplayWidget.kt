package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text

class SkillDisplayWidget(x: Int, y: Int, width: Int, private val skill: RegistryEntry<Skill>, private val level: Int) : ClickableWidget(x, y, width, HEIGHT, Text.empty()) {
    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawItem(skill.value.icon, x + MARGIN, y + MARGIN)
        context.drawText(textRenderer, skill.name, x + 2 * MARGIN + 16, y + MARGIN + 4, 0x404040, false)
        SkillWidget.SKILL_LEVEL.text(level, skill.value.levels.size).let {
            context.drawText(textRenderer, it, x + width - 2 * MARGIN - textRenderer.getWidth(it), y + MARGIN + 4, 0x00FFFF, true)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun playDownSound(soundManager: SoundManager?) {
    }

    companion object {
        const val MARGIN = 2
        const val HEIGHT = MARGIN * 2 + 16

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}