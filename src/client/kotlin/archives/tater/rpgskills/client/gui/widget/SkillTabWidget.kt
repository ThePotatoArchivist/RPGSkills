package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.screen.Tabbed
import archives.tater.rpgskills.util.Translation
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class SkillTabWidget(x: Int, y: Int, private val level: Int, private val parent: Tabbed?) :
    ClickableWidget(x, y, WIDTH, HEIGHT, Text.of(level.toString())) {

    private val text = (level + 1).toString()
    private val tooltip = TOOLTIP.text(level)

    private val isSelectedTab get() = parent?.selectedTab == level

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(if (isSelectedTab) TEXTURE_SELECTED else TEXTURE_UNSELECTED, x, y, 0f, 0f, WIDTH, HEIGHT, WIDTH, HEIGHT)
        context.drawText(textRenderer, text, x + (WIDTH + 1) / 2 - textRenderer.getWidth(text) / 2, y + 7, 0x404040, false)
        if (hovered) context.drawTooltip(textRenderer, tooltip, mouseX, mouseY)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        parent?.selectedTab = level
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

    }

    companion object {
        val TEXTURE_UNSELECTED = RPGSkills.id("textures/gui/sprites/hud/level_tab.png")
        val TEXTURE_SELECTED = RPGSkills.id("textures/gui/sprites/hud/level_tab_selected.png")

        val TOOLTIP = Translation.arg("screen.widget.rpgskills.skilltab.tooltip")

        const val WIDTH = 19
        const val HEIGHT = 19

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}