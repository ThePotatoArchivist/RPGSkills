package archives.tater.rpgskills.client.gui

import archives.tater.rpgskills.RPGSkills
import net.minecraft.client.gui.DrawContext

object SkillXpBar {
    fun draw(context: DrawContext, progress: Float, x: Int, y: Int) {
//        val skills = player[SkillsComponent]

        // Experience Number
//    "${skills.spendableLevels}/${skills.level}".let {
//        context.drawOutlinedText(textRenderer, it, rightX - XP_BAR_WIDTH - 5 - textRenderer.getWidth(it), y, 0x70DACD)
//    }
        // Experience Bar
        context.drawGuiTexture(EMPTY_TEXTURE, x, y, WIDTH, HEIGHT)
        context.drawGuiTexture(
            FILLED_TEXTURE,
            WIDTH,
            HEIGHT,
            0,
            0,
            x,
            y,
            (progress * WIDTH).toInt(),
            HEIGHT
        )
    }

    val EMPTY_TEXTURE = RPGSkills.id("skill/large_bar_empty")
    val FILLED_TEXTURE = RPGSkills.id("skill/large_bar_filled")

    const val WIDTH = 101
    const val HEIGHT = 5
}
