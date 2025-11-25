package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.client.gui.screen.Paged
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.util.Identifier
import javax.swing.Spring.height

class ClassNavButtonWidget(
    parent: Paged,
    x: Int,
    y: Int,
    isNext: Boolean
) : NavButtonWidget(parent, if (isNext) NEXT_TEXTURES else PREV_TEXTURES, x, y, WIDTH, HEIGHT, isNext) {

    companion object {
        val PREV_TEXTURES = ButtonTextures(
            Identifier.ofVanilla("recipe_book/page_backward"),
            Identifier.ofVanilla("recipe_book/page_backward_highlighted")
        )
        val NEXT_TEXTURES = ButtonTextures(
            Identifier.ofVanilla("recipe_book/page_forward"),
            Identifier.ofVanilla("recipe_book/page_forward_highlighted")
        )

        const val WIDTH = 12
        const val HEIGHT = 17
    }
}