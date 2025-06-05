package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.widget.SkillWidget.Companion.BAR_HEIGHT
import archives.tater.rpgskills.client.gui.widget.SkillWidget.Companion.BAR_NOTCH_WIDTH
import archives.tater.rpgskills.client.gui.widget.SkillWidget.Companion.BAR_TEXTURE_WIDTH
import net.minecraft.client.gui.DrawContext

object SkillBar {
    val BAR_TEXTURE = RPGSkills.id("skill/bar_empty")
    val BAR_TEXTURE_FILL = RPGSkills.id("skill/bar_filled")

    const val TEXTURE_WIDTH = 31
    const val NOTCH_WIDTH = 10
    const val HEIGHT = 5

    fun draw(context: DrawContext, x: Int, y: Int, max: Int, level: Int) {
        repeat(max) { i ->
            val first = i == 0
            val last = i + 1 == max
            context.drawGuiTexture(
                if (i < level) SkillWidget.BAR_TEXTURE_FILL else SkillWidget.BAR_TEXTURE,
                BAR_TEXTURE_WIDTH,
                BAR_HEIGHT,
                when {
                    first -> 0
                    last -> 21
                    else -> 11
                },
                0,
                x + i * BAR_NOTCH_WIDTH + if (first) 0 else 1,
                y,
                BAR_NOTCH_WIDTH + if (first) 1 else 0,
                BAR_HEIGHT
            )
        }
    }
}