package archives.tater.rpgskills

import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.joinToText
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.Texts
import net.minecraft.util.Formatting
import java.util.function.Consumer
import kotlin.collections.component1
import kotlin.collections.component2

object RequirementTooltip {
    val REQUIREMENT = Translation.arg("$MOD_ID.tooltip.stack.requirement") { formatted(Formatting.DARK_GRAY) }
    val HINT = Translation.arg("$MOD_ID.tooltip.stack.hint") {
        formatted(Formatting.DARK_GRAY)
    }
    val REQUIRES = Translation.unit("$MOD_ID.tooltip.stack.requires")
    val USE_REQUIRES = Translation.unit("$MOD_ID.tooltip.stack.requires.use")
    val PLACE_REQUIRES = Translation.unit("$MOD_ID.tooltip.stack.requires.place")
    val CRAFT_REQUIRES = Translation.unit("$MOD_ID.tooltip.stack.requires.craft")
    val OR = Translation.unit("$MOD_ID.tooltip.stack.requires.or")

    @JvmStatic
    fun getRequirement(requirement: Collection<Map.Entry<RegistryEntry<Skill>, Int>>, player: PlayerEntity, tooltip: Boolean = true): MutableText {
        val skills = player[SkillsComponent]

        return requirement.toList().joinToText (Text.literal(" + ").formatted(Formatting.DARK_GRAY)) { (skill, level) ->
            Text.empty().apply {
                append(skill.name)
                append(Text.literal(" $level").apply {
                    when {
                        !tooltip -> {}
                        skills[skill] < level -> formatted(Formatting.RED)
                        else -> formatted(Formatting.AQUA)
                    }
                })
                if (tooltip) formatted(Formatting.WHITE)
            }
        }
    }

    fun getTitle(lockGroup: LockGroup, player: PlayerEntity, title: MutableText?, tooltip: Boolean = true): Text =
        (title ?: REQUIRES.text()).apply {
            formatted(
                when {
                    !tooltip -> Formatting.BLACK
                    lockGroup.isSatisfiedBy(player) -> Formatting.GRAY
                    else -> Formatting.RED
                }
            )
        }

    @JvmStatic
    @JvmOverloads
    fun appendRequirements(lockGroup: LockGroup, player: PlayerEntity, text: Consumer<Text>, title: MutableText? = null, tooltip: Boolean = true) {
        text.accept(getTitle(lockGroup, player, title, tooltip))
        lockGroup.requirements.forEachIndexed { index, requirement ->
            text.accept(Text.empty().apply { // Prevent bug with rpg series spell engine
                append(REQUIREMENT.text(getRequirement(requirement.entries, player, tooltip)).apply {
                    if (index + 1 < lockGroup.requirements.size) append(OR.text)
                })
            })
        }
    }

    fun appendRequirements(lockGroup: LockGroup, player: PlayerEntity, text: MutableList<Text>, title: MutableText? = null, tooltip: Boolean = true) {
        appendRequirements(lockGroup, player, text::add, title, tooltip)
    }

    @JvmStatic
    fun appendTooltip(stack: ItemStack, player: PlayerEntity, tooltip: MutableList<Text>, keyPressed: Boolean, keybinding: Text) {
        val registries = player.world.relreg_reloadableRegistries()
        val useLock = LockGroup.useGroupOf(registries, stack)
        val placeLock = LockGroup.placeGroupOf(registries, stack)
        val craftLock = LockGroup.craftGroupOf(registries, stack)

        if (useLock == null && placeLock == null && craftLock == null) return

        if (!keyPressed) {
            tooltip.add(HINT.text(keybinding))
            return
        }

        useLock?.let {
            tooltip.add(Text.empty())
            appendRequirements(it, player, tooltip, USE_REQUIRES.text())
        }
        placeLock?.let {
            tooltip.add(Text.empty())
            appendRequirements(it, player, tooltip, PLACE_REQUIRES.text())
        }
        craftLock?.let {
            tooltip.add(Text.empty())
            appendRequirements(it, player, tooltip, CRAFT_REQUIRES.text())
        }
    }
}
