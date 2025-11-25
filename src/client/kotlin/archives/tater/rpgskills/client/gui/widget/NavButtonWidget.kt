package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.client.gui.screen.Paged
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

open class NavButtonWidget(private val parent: Paged, private val textures: ButtonTextures, x: Int, y: Int, width: Int, height: Int, private val isNext: Boolean) :
    ClickableWidget(x, y, width, height, Text.empty()) {

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawGuiTexture(textures.get(true, isHovered), x, y, width, height)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (isNext)
            parent.selectedPage++
        else
            parent.selectedPage--
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }
}