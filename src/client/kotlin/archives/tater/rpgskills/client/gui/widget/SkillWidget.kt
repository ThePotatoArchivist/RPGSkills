package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.client.gui.SkillXpBar
import archives.tater.rpgskills.client.gui.screen.SkillScreen
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.description
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry

class SkillWidget(
    x: Int,
    y: Int,
    private val player: PlayerEntity,
    private val skill: RegistryEntry<Skill>,
    private val parent: Screen? = null,
) : ClickableWidget(x, y, WIDTH - SkillUpgradeButton.WIDTH - 5, HEIGHT, skill.name) {
    private val skillsComponent = player[SkillsComponent]
    private val name = skill.name
    private val description = skill.description

    private val level get() = skillsComponent[skill]
    private val maxLevel = skill.value.levels.size

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawGuiTexture(if (hovered) TEXTURE_HIGHLIGHTED else TEXTURE, x, y, WIDTH, HEIGHT)
        context.drawItem(skill.value.icon, x + 3, y + 3)
        context.drawText(textRenderer, name, x + 21, y + 4, 0xffffff, true)

        SkillXpBar.draw(context, level.toFloat() / maxLevel, x + 21, y + 14, skillsComponent.isPointsFull && skillsComponent.spendableLevels <= 0)

        if (hovered)
            MinecraftClient.getInstance().currentScreen?.setTooltip(listOf(
                description.asOrderedText(),
                SKILL_LEVEL.text(level, maxLevel).withColor(0x00ffff).asOrderedText(),
            ))
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        val client = MinecraftClient.getInstance()
        client.setScreen(SkillScreen(player, skill, parent))
    }

    companion object {
        val TEXTURE = RPGSkills.id("skill/entry")
        val TEXTURE_HIGHLIGHTED = RPGSkills.id("skill/entry_highlighted")

        val SKILL_LEVEL = Translation.arg("screen.widget.$MOD_ID.skill.level")

        const val WIDTH = 146
        const val HEIGHT = 22

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}
