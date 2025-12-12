package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.widget.AvailableJobWidget.Companion.TEXTURE
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.predicate.entity.DistancePredicate.y
import net.minecraft.text.Text
import com.ibm.icu.impl.ValidIdentifiers

abstract class AbstractAvailableJobWidget(x: Int, y: Int) : ClickableWidget(x, y, WIDTH, HEIGHT, Text.empty()), AbstractJobWidget {
    abstract val isHighlighted: Boolean
    abstract val isDisabled: Boolean

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawGuiTexture(TEXTURE[!isDisabled, isHighlighted], x, y, WIDTH, HEIGHT)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    companion object {
        const val WIDTH = 128
        const val HEIGHT = 18
        const val MARGIN = 4

        val TEXTURE = ButtonTextures(
            RPGSkills.id("skill/job_option"),
            RPGSkills.id("skill/job_option_disabled"),
            RPGSkills.id("skill/job_option_highlighted"),
        )
    }
}