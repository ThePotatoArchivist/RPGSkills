package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.client.gui.widget.*
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.client.gui.SkillXpBar
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.streamEntriesOrdered
import archives.tater.rpgskills.util.value
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.screen.ScreenTexts

class SkillScreen(
    private val player: PlayerEntity,
    private val skill: RegistryEntry<Skill>,
    private val parent: Screen? = null,
) : Screen(skill.name) {
    private var x = 0
    private var y = 0

    val skills = player[SkillsComponent]
    private val maxLevel = skill.value.levels.size

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        addDrawableChild(SkillUpgradeButton(x + WIDTH - SkillUpgradeButton.WIDTH - 8, y + 5, player, skill))

        val scrollContents = buildList {
            skill.value.levels.forEachIndexed { index, level ->
                val levelAmount = index + 1
                var hasContent = false

                add(TextWidget(x + 10, 0, 224, textRenderer.fontHeight + 8, LEVEL.text(levelAmount), textRenderer))

                level.attributes.takeIf { it.isNotEmpty() }?.let {
                    add(AttributesWidget(x + 10, 0, 224, it))
                    hasContent = true
                }

                for (job in level.jobs) {
                    add(JobUnlockWidget(x + 10, 0, 224, job))
                    hasContent = true
                }

                player.registryManager[LockGroup].streamEntriesOrdered(RPGSkillsTags.LOCK_GROUP_ORDER)
                    .filter { lockEntry -> lockEntry.value.requirements.any { it[skill] == levelAmount } }
                    .forEach {
                        add(LockGroupWidget(x + 10, 0, 224, it.value, player.registryManager, player.world.recipeManager))
                        hasContent = true
                    }

                if (!hasContent)
                    removeLast()
            }
        }

        addDrawableChild(AutoScrollingWidget(x + 9, y + 26, 234, 141, scrollContents))

        addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK) { close() }.apply {
            width(200)
            position(client!!.window.scaledWidth / 2 - 100, client!!.window.scaledHeight - 25)
        }.build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawItem(skill.value.icon, x + 8, y + 6)
        context.drawText(textRenderer, title, x + 26, y + 10, 0x404040, false)

        SkillXpBar.draw(context, skills[skill].toFloat() / maxLevel, x + WIDTH - SkillUpgradeButton.WIDTH - 8 - 4 - SkillXpBar.WIDTH, y + 12)

        SkillWidget.SKILL_LEVEL.text(skills[skill], maxLevel).let {
            context.drawText(textRenderer, it, x + WIDTH - SkillUpgradeButton.WIDTH - 8 - 4 - SkillXpBar.WIDTH - 4 - textRenderer.getWidth(it), y + 10, 0x404040, false)
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
        const val HEIGHT = 176

        val LEVEL = Translation.arg("screen.rgpskills.skill.level")
    }
}
