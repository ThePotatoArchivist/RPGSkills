package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.widget.SkillUpgradeButton
import archives.tater.rpgskills.client.gui.widget.SkillWidget
import archives.tater.rpgskills.client.util.drawOutlinedText
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.util.get
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

class SkillsScreen(private val player: PlayerEntity) : Screen(Text.translatable(NAME_TRANSLATION)) {
    private var x = 0;
    private var y = 0;

    override fun shouldPause() = false

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        player[SkillsComponent].levels.entries.forEachIndexed { index, (skill, level) ->
            addDrawableChild(SkillWidget(x + 9, y + index * SkillWidget.HEIGHT + 18, player, skill))
            addDrawableChild(SkillUpgradeButton(x + 197, y + index * SkillWidget.HEIGHT + 18 + 2, player, skill))
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        context.drawTexture(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT)
        context.drawText(textRenderer, title, x + 8, y + 6, 0x404040, false)
        "${player[SkillsComponent].remainingLevelPoints}/${player.experienceLevel}".let {
            context.drawOutlinedText(textRenderer, it, x + 139 - it.length * 6, y + 5)
        }
        context.drawTexture(TEXTURE, x + 143, y + 8, 143, 141, (player.experienceProgress * XP_BAR_WIDTH).toInt(), XP_BAR_HEIGHT)
        super.render(context, mouseX, mouseY, delta)
    }

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/skills.png")
        const val WIDTH = 252
        const val HEIGHT = 140

        const val XP_BAR_WIDTH = 101
        const val XP_BAR_HEIGHT = 5

        const val NAME_TRANSLATION = "screen.rpgskills.skills"
    }
}
