package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.client.gui.screen.SkillsScreen
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.description
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.sound.SoundManager
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry

class SkillWidget(
    x: Int,
    y: Int,
    player: PlayerEntity,
    private val skill: RegistryEntry<Skill>,
) : ClickableWidget(x, y, WIDTH - SkillUpgradeButton.WIDTH - 5, HEIGHT, skill.name) {
    private val skillsComponent = player[SkillsComponent]
    private val name = skill.name
    private val description = skill.description

    private val level get() = skillsComponent[skill]
    private val maxLevel = skill.value.levels.size

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(TEXTURE, x, y, 9, if (isHovered) 147 + HEIGHT else 147, WIDTH, HEIGHT)
        context.drawItem(skill.value.icon, x + 3, y + 3)
        context.drawText(textRenderer, name, x + 21, y + 4, 0xffffff, true)

        val level = level
        val maxLevel = maxLevel
        repeat(maxLevel) { i ->
            val first = i == 0
            context.drawTexture(
                TEXTURE,
                x + 21 + i * BAR_NOTCH_WIDTH + if (first) 0 else 1,
                y + 14,
                when {
                    first -> 29
                    i + 1 == maxLevel -> 50
                    else -> 40
                },
                if (i < level) 198 else 192,
                if (first) BAR_NOTCH_WIDTH + 1 else BAR_NOTCH_WIDTH,
                BAR_HEIGHT
            )
        }
        if (hovered)
            context.drawTooltip(textRenderer, description, mouseX, mouseY)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    override fun onClick(mouseX: Double, mouseY: Double) {

    }

    override fun playDownSound(soundManager: SoundManager?) {

    }

    companion object {
        val TEXTURE = SkillsScreen.TEXTURE
        const val WIDTH = 227
        const val HEIGHT = 22

        const val BAR_NOTCH_WIDTH = 10
        const val BAR_HEIGHT = 5

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}
