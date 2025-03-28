package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.widget.SkillWidget
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.util.get
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.NarratorManager
import net.minecraft.entity.player.PlayerEntity

class SkillsScreen(private val player: PlayerEntity) : Screen(NarratorManager.EMPTY) {
    private var x = 0;
    private var y = 0;

    override fun shouldPause() = false

    override fun init() {
        player[SkillsComponent].levels.entries.forEachIndexed { index, (skill, level) ->
            addDrawableChild(SkillWidget(0, index * 18, 200, 18, player, skill, level))
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        context.drawTexture(TEXTURE, )
        super.render(context, mouseX, mouseY, delta)
        player[SkillsComponent].levels.entries.forEachIndexed { index, (skill, level) ->
            context.drawCenteredTextWithShadow(textRenderer, skill.name, width / 2, height / 2 + index * 18, 0xffffff);
        }
    }

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/skills.png")
        const val WIDTH = 252
        const val HEIGHT = 140
    }
}
