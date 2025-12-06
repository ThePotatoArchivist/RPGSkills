package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.client.gui.widget.*
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.streamEntriesOrdered
import archives.tater.rpgskills.util.value
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
) : Screen(skill.name), Tabbed, Paged {
    private var x = 0
    private var y = 0
    override var selectedTab = 0
        set(value) {
            field = value
            clearAndInit()
        }

    private val maxLevel = skill.value.levels.size

    private val tabPages = maxLevel ceilDiv MAX_TABS
    override var selectedPage: Int = 0 // Tabs
        set(value) {
            field = value.mod(tabPages)
            clearAndInit()
        }
    private val tabOffset get() = selectedPage * MAX_TABS

    private inline val selectedLevel get() = selectedTab + 1

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        repeat((maxLevel - tabOffset).coerceAtMost(MAX_TABS)) {
            addDrawableChild(SkillTabWidget(x + it * 20 + 6, y, tabOffset + it, this))
        }

        if (maxLevel > MAX_TABS) {
            addDrawableChild(TabNavButtonWidget(this, x - TabNavButtonWidget.WIDTH + 4, y + 3, false))
            addDrawableChild(TabNavButtonWidget(this, x + WIDTH - 5, y + 3, true))
        }

        addDrawableChild(SkillUpgradeButton(x + WIDTH - SkillUpgradeButton.WIDTH - 8, y + 21, player, skill))

        val scrollContents = buildList {
            skill.value.levels[selectedTab].attributes.takeIf { it.isNotEmpty() }?.let {
                add(AttributesWidget(x + 10, 0, 224, it))
            }

            for (job in skill.value.levels[selectedTab].jobs) {
                add(SkillJobWidget(x + 10, 0, 224, job))
            }

            player.registryManager[LockGroup].streamEntriesOrdered(RPGSkillsTags.LOCK_GROUP_ORDER)
                .filter { lockEntry -> lockEntry.value.requirements.any { it[skill] == selectedLevel } }
                .forEach {
                    add(LockGroupWidget(x + 10, 0, 224, it.value, player.registryManager, player.world.recipeManager))
                }
        }

        addDrawableChild(AutoScrollingWidget(x + 9, y + 42, 234, 141, scrollContents))

        addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK) { close() }.apply {
            width(200)
            position(client!!.window.scaledWidth / 2 - 100, client!!.window.scaledHeight - 25)
        }.build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawItem(skill.value.icon, x + 8, y + 22)
        context.drawText(textRenderer, title, x + 26, y + 26, 0x404040, false)

        SkillWidget.SKILL_LEVEL.text(player[SkillsComponent][skill], maxLevel).let {
            context.drawText(textRenderer, it, x + WIDTH - SkillUpgradeButton.WIDTH - 8 - 4 - textRenderer.getWidth(it), y + 26, 0x00FFFF, true)
        }
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

        const val MAX_TABS = 12
    }
}
