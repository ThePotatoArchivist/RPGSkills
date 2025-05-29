package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.client.gui.widget.SkillUpgradeButton
import archives.tater.rpgskills.client.gui.widget.SkillWidget
import archives.tater.rpgskills.client.util.drawOutlinedText
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper.clamp

class SkillsScreen(private val player: PlayerEntity) : Screen(TITLE.text) {
    private var x = 0
    private var y = 0
    private lateinit var skillWidgets: List<Pair<SkillWidget, SkillUpgradeButton>>
    private var indexOffset = 0
    private val canScroll get() = skillWidgets.size > MAX_VISIBLE
    private var scrolling = false

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        skillWidgets = player.registryManager[Skill].streamEntries().toList().mapIndexed { index, skill ->
            Pair(
                addDrawableChild(SkillWidget(x + 9, y + index * SkillWidget.HEIGHT + 19, player, skill)),
                addDrawableChild(SkillUpgradeButton(x + 197, y + index * SkillWidget.HEIGHT + 19 + 2, player, skill))
            )
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context, mouseX, mouseY, delta)
        // Background
        context.drawTexture(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT)
        // Title
        context.drawText(textRenderer, title, x + 8, y + 7, 0x404040, false)
        // Experience Number
        val skills = player[SkillsComponent]

        "${skills.spendableLevels}/${skills.level}".let {
            context.drawOutlinedText(textRenderer, it, x + 139 - it.length * 6, y + 6, 0x70DACD)
        }
        // Experience Bar
        context.drawTexture(TEXTURE, x + 143, y + 8, 143, 141, (skills.levelProgress * XP_BAR_WIDTH).toInt(), XP_BAR_HEIGHT)
        // List
        (if (skillWidgets.size > MAX_VISIBLE) skillWidgets.subList(indexOffset, indexOffset + MAX_VISIBLE) else skillWidgets)
            .forEachIndexed { index, (widget, button) ->
                val widgetY = y + index * SkillWidget.HEIGHT + 19
                widget.y = widgetY
                button.y = widgetY + 2
                widget.render(context, mouseX, mouseY, delta)
                button.render(context, mouseX, mouseY, delta)
            }
        // Scrollbar
        context.drawTexture(
            TEXTURE,
            x + SCROLLBAR_X,
            y + SCROLLBAR_Y + (SCROLL_HEIGHT - SCROLLBAR_HEIGHT) * indexOffset / (skillWidgets.size - MAX_VISIBLE),
            if (canScroll) 237 else 243,
            147,
            SCROLL_WIDTH,
            SCROLLBAR_HEIGHT
        )
    }

    // based on MerchantScreen

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (canScroll)
            indexOffset = clamp((indexOffset.toDouble() - verticalAmount).toInt(), 0, skillWidgets.size - MAX_VISIBLE)

        return true
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        scrolling = canScroll
                && mouseX > x + SCROLLBAR_X && mouseX < x + SCROLLBAR_X + SCROLL_WIDTH
                && mouseY > y + SCROLLBAR_Y && mouseY < y + SCROLLBAR_Y + SCROLL_HEIGHT

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (!scrolling)
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)

        val scrollStart = y + SCROLLBAR_Y
        val scrollEnd = scrollStart + SCROLL_HEIGHT
        val amountHidden = skillWidgets.size - MAX_VISIBLE
        val f = (mouseY.toFloat() - scrollStart.toFloat() - SCROLLBAR_HEIGHT / 2f) / ((scrollEnd - scrollStart).toFloat() - SCROLLBAR_HEIGHT)
        val g = f * amountHidden.toFloat() + 0.5f
        this.indexOffset = clamp(g.toInt(), 0, amountHidden)
        return true
    }

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/skills.png")
        const val WIDTH = 252
        const val HEIGHT = 138

        const val XP_BAR_WIDTH = 101
        const val XP_BAR_HEIGHT = 5

        const val MAX_VISIBLE = 5
        const val SCROLL_WIDTH = 6
        const val SCROLL_HEIGHT = 110
        const val SCROLLBAR_HEIGHT = 27
        const val SCROLLBAR_X = 237
        const val SCROLLBAR_Y = 19

        val TITLE = Translation.unit("screen.$MOD_ID.skills")
    }
}
