package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.client.gui.screen.Paged
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class NavButtonWidget(private val parent: Paged, x: Int, y: Int, private val isNext: Boolean) :
    ClickableWidget(x, y, WIDTH, HEIGHT, Text.empty()) {

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawGuiTexture((if (isNext) NEXT_TEXTURES else PREV_TEXTURES).get(true, isHovered), x, y, WIDTH, HEIGHT)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (isNext)
            parent.selectedPage++
        else
            parent.selectedPage--
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

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