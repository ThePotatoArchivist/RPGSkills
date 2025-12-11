package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.client.gui.SkillXpBar
import archives.tater.rpgskills.client.gui.widget.SkillUpgradeButton
import archives.tater.rpgskills.client.gui.widget.SkillWidget
import archives.tater.rpgskills.client.util.drawOutlinedText
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
        context.drawText(textRenderer, title, x + (WIDTH - textRenderer.getWidth(title)) / 2, y + MARGIN - 2, 0x404040, false)

        context.drawText(textRenderer, LEVEL.text(skills.level), x + MARGIN, y + MARGIN + 10, 0x404040, false)

        val levelString = skills.spendableLevels.toString()
        context.drawOutlinedText(textRenderer, levelString, x + (WIDTH - textRenderer.getWidth(levelString) - 2) / 2, y + MARGIN + 9, 0x70DACD)

        val progressText = PROGRESS.text(skills.remainingPoints, skills.pointsInLevel)
        context.drawText(textRenderer, progressText, x + WIDTH - MARGIN - textRenderer.getWidth(progressText), y + MARGIN + 10, 0x404040, false)

        SkillXpBar.draw(context, skills.levelProgress, x + (WIDTH - SkillXpBar.WIDTH) / 2, y + 29)
    }

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/skills.png")
        const val WIDTH = 164
        const val HEADER_HEIGHT = 39
        const val SEGMENT_HEIGHT = 22
        const val FOOTER_HEIGHT = 9
        const val MARGIN = 9

        val TITLE = Translation.unit("screen.$MOD_ID.skills")

        val LEVEL = Translation.arg("screen.$MOD_ID.skills.level")
        val PROGRESS = Translation.arg("screen.$MOD_ID.skills.progress")

    }
}
