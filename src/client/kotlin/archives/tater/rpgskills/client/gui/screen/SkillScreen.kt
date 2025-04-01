package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.widget.SkillUpgradeButton
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.value
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry

@Environment(EnvType.CLIENT)
class SkillScreen(
    private val player: PlayerEntity,
    private val skill: RegistryEntry<Skill>,
    private val parent: Screen?,
) : Screen(skill.name) {
    private var x = 0
    private var y = 0

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        addDrawableChild(SkillUpgradeButton(x + 48, y + 8, player, skill))
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        context.drawItem(skill.value.icon, x + 8, y + 8)
        context.drawText(textRenderer, title, x + 26, y + 8, 0x404040, false)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/skill.png")
        const val WIDTH = 64
        const val HEIGHT = 64
    }
}