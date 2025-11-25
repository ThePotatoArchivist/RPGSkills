package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.screen.Paged
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.util.Identifier
import javax.swing.Spring.height

class TabNavButtonWidget(
    parent: Paged,
    x: Int,
    y: Int,
    isNext: Boolean
) : NavButtonWidget(parent, if (isNext) NEXT_TEXTURES else PREV_TEXTURES, x, y, WIDTH, HEIGHT, isNext) {

    companion object {
        val PREV_TEXTURES = ButtonTextures(
            RPGSkills.id("skill/tab_left"),
            RPGSkills.id("skill/tab_left_highlighted")
        )
        val NEXT_TEXTURES = ButtonTextures(
            RPGSkills.id("skill/tab_right"),
            RPGSkills.id("skill/tab_right_highlighted")
        )

        const val WIDTH = 9
        const val HEIGHT = 11
    }
}