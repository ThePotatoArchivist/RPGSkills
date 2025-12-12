package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.screen.Tabbed
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.advancement.criterion.ConstructBeaconCriterion.Conditions.level
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text

class SkillTabWidget(x: Int, y: Int, private val skill: RegistryEntry<Skill>, private val index: Int, private val parent: Tabbed?) :
    ClickableWidget(x, y, WIDTH, HEIGHT, Text.empty()) {

    private val tooltip = Text.literal(skill.value.name)
    private val isSelectedTab get() = parent?.selectedTab == index

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawGuiTexture(if (isSelectedTab) TEXTURE_SELECTED else TEXTURE_UNSELECTED, x, y, WIDTH, HEIGHT)
        context.drawItem(skill.value.icon, x + 6, y + 5)
        if (hovered)
            context.drawTooltip(textRenderer, tooltip, mouseX, mouseY)
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        parent?.selectedTab = index
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

    }

    companion object {
        val TEXTURE_UNSELECTED = RPGSkills.id("skill/level_tab")
        val TEXTURE_SELECTED = RPGSkills.id("skill/level_tab_selected")

        val TOOLTIP = Translation.arg("screen.widget.rpgskills.skilltab.tooltip")

        const val WIDTH = 28
        const val HEIGHT = 31

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}
