package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.widget.*
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import archives.tater.rpgskills.util.withFirst
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.screen.ScreenTexts

class SkillScreen(
    private val player: PlayerEntity,
    private val skill: RegistryEntry<Skill>,
    private val parent: Screen? = null,
) : Screen(skill.name), Tabbed {
    private var x = 0
    private var y = 0
    override var selectedTab = 0
        set(value) {
            field = value
            clearAndInit()
        }

    private inline val selectedLevel get() = selectedTab + 1

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        repeat(skill.value.levels.size) {
            addDrawableChild(SkillTabWidget(x + it * 20 + 6, y, it, this))
        }

        addDrawableChild(SkillUpgradeButton(x + WIDTH - SkillUpgradeButton.WIDTH - 8, y + 21, player, skill))

        val attributesWidget = skill.value.levels[selectedTab].attributes.takeIf { it.isNotEmpty() }?.let {
            AttributesWidget(x + 10, 0, 224, it)
        }

        val lockWidgets: List<LockGroupWidget> = player.registryManager[LockGroup].streamEntries()
            .filter { lockEntry -> lockEntry.value.requirements.any { it[skill] == selectedLevel } }
            .map {
                LockGroupWidget(x + 10, 0, 224, it.value, player.registryManager, player.world.recipeManager)
            }
            .toList()

        addDrawableChild(AutoScrollingWidget(x + 9, y + 42, 234, 141, attributesWidget?.let(lockWidgets::withFirst) ?: lockWidgets))

        addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK) { close() }.apply {
            width(200)
            position(client!!.window.scaledWidth / 2 - 100, client!!.window.scaledHeight - 25)
        }.build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawItem(skill.value.icon, x + 8, y + 22)
        context.drawText(textRenderer, title, x + 26, y + 23, 0x404040, false)
        val max = skill.value.levels.size
        SkillBar.draw(context, x + 26, y + 32, max, player[SkillsComponent][skill])
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
        const val HEIGHT = 192
    }
}
