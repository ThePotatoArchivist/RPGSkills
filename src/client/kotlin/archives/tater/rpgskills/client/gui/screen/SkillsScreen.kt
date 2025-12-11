package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.cca.BossTrackerComponent
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.client.gui.SkillXpBar
import archives.tater.rpgskills.client.gui.widget.AbstractJobWidget.Companion.ORB_ICON
import archives.tater.rpgskills.client.gui.widget.AbstractJobWidget.Companion.ORB_ICON_SIZE
import archives.tater.rpgskills.client.gui.widget.SkillUpgradeButton
import archives.tater.rpgskills.client.gui.widget.SkillWidget
import archives.tater.rpgskills.client.util.drawOutlinedText
import archives.tater.rpgskills.client.util.mouseIn
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.streamEntriesOrdered
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.ScreenTexts

class SkillsScreen(private val player: PlayerEntity) : AbstractSkillsScreen(player, TITLE.text) {
    private var x = 0
    private var y = 0

    private val skills = player[SkillsComponent]
    private val allSkills = player.registryManager[Skill].streamEntriesOrdered(RPGSkillsTags.SKILL_ORDER).toList()

    private var screenHeight = HEADER_HEIGHT + allSkills.size * SEGMENT_HEIGHT + FOOTER_HEIGHT

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - screenHeight) / 2

        allSkills.forEachIndexed { index, skill ->
            addDrawableChild(SkillWidget(x + MARGIN, y + index * SkillWidget.HEIGHT + HEADER_HEIGHT, player, skill, this))
            addDrawableChild(SkillUpgradeButton(x + MARGIN + SkillWidget.WIDTH - SkillUpgradeButton.WIDTH - 2, y + index * SkillWidget.HEIGHT + HEADER_HEIGHT + 2, player, skill))
        }

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE) { close() }.apply {
            width(200)
            position(width / 2 - 100, height - 25)
        }.build())
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta)
        var y = y
        context.drawTexture(TEXTURE, x, y, 0, 0, WIDTH, HEADER_HEIGHT)
        y += HEADER_HEIGHT
        repeat(allSkills.size) {
            context.drawTexture(TEXTURE, x, y, 0, HEADER_HEIGHT, WIDTH, SEGMENT_HEIGHT)
            y += SEGMENT_HEIGHT
        }
        context.drawTexture(TEXTURE, x, y, 0, HEADER_HEIGHT + SEGMENT_HEIGHT, WIDTH, FOOTER_HEIGHT)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawText(textRenderer, title, x + (WIDTH - textRenderer.getWidth(title)) / 2, y + MARGIN, 0x404040, false)

        val pointsFull = skills.isPointsFull

        val levelText = LEVEL.text(skills.level)
        context.drawText(textRenderer, levelText, x + (WIDTH - textRenderer.getWidth(levelText)) / 2, y + MARGIN + 15, 0x404040, false)

        val spendableString = skills.spendableLevels.toString()
        context.drawOutlinedText(textRenderer, spendableString, x + (WIDTH - textRenderer.getWidth(spendableString) - 2) / 2, y + MARGIN + 32, if (pointsFull && skills.spendableLevels <= 0) 0xb2b2b2 else 0x70DACD)

        context.drawGuiTexture(LEVEL_CAP_ICON, x + MARGIN, y + MARGIN + 32, LEVEL_CAP_ICON_SIZE, LEVEL_CAP_ICON_SIZE)
        val levelCapString = player.world[BossTrackerComponent].maxLevel.toString()
        context.drawText(textRenderer, levelCapString, x + MARGIN + LEVEL_CAP_ICON_SIZE + 2, y + MARGIN + 33, 0x404040, false)
        if (mouseIn(mouseX, mouseY, x + MARGIN, y + MARGIN + 33, LEVEL_CAP_ICON_SIZE + 2 + textRenderer.getWidth(levelCapString), textRenderer.fontHeight))
            setTooltip(LEVEL_CAP_HINT.text)

        val progressText = PROGRESS.text(skills.remainingPoints, skills.pointsInLevel)
        context.drawGuiTexture(ORB_ICON, x + WIDTH - MARGIN - textRenderer.getWidth(progressText) - ORB_ICON_SIZE - 2, y + MARGIN + 32, ORB_ICON_SIZE, ORB_ICON_SIZE)
        context.drawText(textRenderer, progressText, x + WIDTH - MARGIN - textRenderer.getWidth(progressText), y + MARGIN + 33, 0x404040, false)

        SkillXpBar.draw(context, if (pointsFull) 1f else skills.levelProgress, x + (WIDTH - SkillXpBar.WIDTH) / 2, y + MARGIN + 25, pointsFull)
    }

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/skills.png")
        val LEVEL_CAP_ICON = RPGSkills.id("skill/level_cap")

        const val WIDTH = 164
        const val HEADER_HEIGHT = 55
        const val SEGMENT_HEIGHT = 22
        const val FOOTER_HEIGHT = 9
        const val MARGIN = 9
        const val LEVEL_CAP_ICON_SIZE = 9

        val TITLE = Translation.unit("screen.$MOD_ID.skills")

        val LEVEL = Translation.arg("screen.$MOD_ID.skills.level")
        val PROGRESS = Translation.arg("screen.$MOD_ID.skills.progress")
        val LEVEL_CAP_HINT = Translation.unit("screen.$MOD_ID.skills.level_cap_hint")
    }
}
