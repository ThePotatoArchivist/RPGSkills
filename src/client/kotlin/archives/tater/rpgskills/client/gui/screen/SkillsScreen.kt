package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.client.gui.widget.SkillWidget
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.util.get
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.NarratorManager
import net.minecraft.entity.player.PlayerEntity

class SkillsScreen(private val player: PlayerEntity) : Screen(NarratorManager.EMPTY) {

    override fun shouldPause() = false

    override fun init() {
        player[SkillsComponent].levels.entries.forEachIndexed { index, (skill, level) ->
            addDrawableChild(SkillWidget(0, index * 18, 200, 18, player, skill, level))
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        player[SkillsComponent].levels.entries.forEachIndexed { index, (skill, level) ->
            context.drawCenteredTextWithShadow(textRenderer, skill.name, width / 2, height / 2 + index * 18, 0xffffff);
        }
    }
}