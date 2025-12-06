package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.util.drawOutlinedText
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.util.get
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

abstract class AbstractSkillsScreen(private val player: PlayerEntity, title: Text) : Screen(title) {

    protected fun drawXpBar(context: DrawContext, rightX: Int, y: Int) {
        val skills = player[SkillsComponent]

        // Experience Number
        "${skills.spendableLevels}/${skills.level}".let {
            context.drawOutlinedText(textRenderer, it, rightX - XP_BAR_WIDTH - 5 - textRenderer.getWidth(it), y, 0x70DACD)
        }
        // Experience Bar
        context.drawGuiTexture(BAR_EMPTY_TEXTURE, rightX - XP_BAR_WIDTH, y + 2, XP_BAR_WIDTH, XP_BAR_HEIGHT)
        context.drawGuiTexture(BAR_FILLED_TEXTURE, XP_BAR_WIDTH, XP_BAR_HEIGHT, 0, 0, rightX - XP_BAR_WIDTH, y + 2, (skills.levelProgress * XP_BAR_WIDTH).toInt(), XP_BAR_HEIGHT)
    }

    companion object {
        val BAR_EMPTY_TEXTURE = RPGSkills.id("skill/large_bar_empty")
        val BAR_FILLED_TEXTURE = RPGSkills.id("skill/large_bar_filled")

        const val XP_BAR_WIDTH = 101
        const val XP_BAR_HEIGHT = 5
    }
}
