package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.client.gui.widget.SkillWidget
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.util.get
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.NarratorManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import kotlin.jvm.optionals.getOrNull

class SkillsScreen(private val player: PlayerEntity) : Screen(NarratorManager.EMPTY) {

    private val skillRegistry = player.world.registryManager[Skill]

    override fun shouldPause() = false

    override fun init() {
        player[SkillsComponent].levels.entries.forEachIndexed { index, (skillKey, level) ->
            val skill = skillRegistry.getEntry(skillKey).get()
            addDrawableChild(SkillWidget(0, index * 18, 200, 18, player, skill, level))
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        player[SkillsComponent].levels.entries.forEachIndexed { index, (key, value) ->
            context.drawCenteredTextWithShadow(textRenderer, player.world.registryManager[Skill].getEntry(key).getOrNull()?.name ?: Text.empty(), width / 2, height / 2 + index * 18, 0xffffff);
        }
    }
}