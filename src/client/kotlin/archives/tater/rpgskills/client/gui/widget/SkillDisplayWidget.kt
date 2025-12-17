package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.client.gui.SkillXpBar
import archives.tater.rpgskills.client.gui.widget.SkillWidget.Companion.SKILL_LEVEL
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text

class SkillDisplayWidget(x: Int, y: Int, private val skill: RegistryEntry<Skill>, private val level: Int) : ClickableWidget(x, y, WIDTH, HEIGHT, Text.empty()) {
    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
//        context.drawGuiTexture(ActiveJobWidget.BACKGROUND_TEXTURE, x, y, width, height)
        context.drawItem(skill.value.icon, x + MARGIN, y + MARGIN)
        context.drawText(textRenderer, skill.name, x + MARGIN + 2 + 16, y + MARGIN, 0x404040, false)
        SKILL_LEVEL.text(level, skill.value.levels.size).let {
            context.drawText(textRenderer, it, x + width - MARGIN - 2 - textRenderer.getWidth(it), y + MARGIN, 0x00FFFF, true)
        }
        SkillXpBar.draw(context, level.toFloat() / skill.value.levels.size, x + MARGIN + 2 + 16, y + height - MARGIN - SkillXpBar.HEIGHT)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {
    }

    override fun playDownSound(soundManager: SoundManager?) {
    }

    companion object {
        const val MARGIN = 4
        const val HEIGHT = MARGIN * 2 + 16
        const val WIDTH = 127

        val SKILL_LEVEL = Translation.arg("screen.widget.$MOD_ID.skill_display.level")

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}