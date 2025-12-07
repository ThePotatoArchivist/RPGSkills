package archives.tater.rpgskills.client.gui.widget

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ScrollableWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.text.Text

class AutoScrollingWidget<T>(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val gap: Int,
    private val contents: List<T>,
) : ScrollableWidget(x, y, width - SCROLLER_WIDTH, height, Text.empty()) where T: Widget, T: Drawable {

    constructor(x: Int, y: Int, width: Int, height: Int, contents: List<T>) : this(x, y, width, height, 2, contents)

    private val contentsHeight = contents.fold(1) { currentY, widget ->
        widget.y = y + currentY
        currentY + widget.height + gap
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {}

    override fun getContentsHeight(): Int = contentsHeight

    override fun getDeltaYPerScroll(): Double = 9.0

    override fun drawBox(context: DrawContext?) {}

    override fun renderContents(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        for (drawable in contents)
            drawable.render(context, mouseX, mouseY, delta)
    }

    companion object {
        const val SCROLLER_WIDTH = 8
    }
}