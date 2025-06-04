package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.widget.SkillTabWidget
import archives.tater.rpgskills.client.gui.widget.SkillUpgradeButton
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.value
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry

class SkillScreen(
    private val player: PlayerEntity,
    private val skill: RegistryEntry<Skill>,
    private val parent: Screen? = null,
) : Screen(skill.name), Tabbed {
    private var x = 0
    private var y = 0
    override var selectedTab = 0

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        repeat(skill.value().levels.size) {
            addDrawableChild(SkillTabWidget(x + it * 20 + 6, y, it, this))
        }

        addDrawableChild(SkillUpgradeButton(x + WIDTH - SkillUpgradeButton.WIDTH - 8, y + 20, player, skill))
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawItem(skill.value.icon, x + 8, y + 21)
        context.drawText(textRenderer, title, x + 26, y + 25, 0x404040, false)
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta)
        context.drawTexture(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/skill.png")

        const val WIDTH = 252
        const val HEIGHT = 154
    }
}
