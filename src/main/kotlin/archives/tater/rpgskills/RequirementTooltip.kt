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
import net.minecraft.util.Formatting
import java.util.function.Consumer
import kotlin.collections.component1
import kotlin.collections.component2

object RequirementTooltip {
    val REQUIRES = Translation.unit("$MOD_ID.tooltip.stack.requires")
    val REQUIRES_ANY = Translation.unit("$MOD_ID.tooltip.stack.requires.any")
    val REQUIREMENT = Translation.arg("$MOD_ID.tooltip.stack.requirement") { formatted(Formatting.DARK_GRAY) }
    val HINT = Translation.unit("$MOD_ID.tooltip.stack.hint") {
        formatted(Formatting.DARK_GRAY)
    }
    val USE = Translation.unit("$MOD_ID.tooltip.stack.use")
    val PLACE = Translation.unit("$MOD_ID.tooltip.stack.place")
    val CRAFT = Translation.unit("$MOD_ID.tooltip.stack.craft")

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

    fun getTitle(lockGroup: LockGroup, player: PlayerEntity, prefix: Text? = null, tooltip: Boolean = true): Text =
        (if (lockGroup.requirements.size == 1) REQUIRES.text() else REQUIRES_ANY.text()).let {
            if (prefix == null) it else ((prefix as? MutableText) ?: prefix.copy()).append(it)
        }.apply {
            formatted(
                when {
                    !tooltip -> Formatting.BLACK
                    lockGroup.isSatisfiedBy(player) -> Formatting.WHITE
                    else -> Formatting.RED
                }
            )
        }

    @JvmStatic
    @JvmOverloads
    fun appendRequirements(lockGroup: LockGroup, player: PlayerEntity, text: Consumer<Text>, prefix: Text? = null, tooltip: Boolean = true) {
        text.accept(getTitle(lockGroup, player, prefix, tooltip))
        for (requirement in lockGroup.requirements)
            text.accept(REQUIREMENT.text(getRequirement(requirement.entries, player, tooltip)))
    }

    fun appendRequirements(lockGroup: LockGroup, player: PlayerEntity, text: MutableList<Text>, prefix: Text? = null, tooltip: Boolean = true) {
        appendRequirements(lockGroup, player, text::add, prefix, tooltip)
    }

    @JvmStatic
    fun appendTooltip(stack: ItemStack, player: PlayerEntity, tooltip: MutableList<Text>, keyPressed: Boolean) {
        val useLock = LockGroup.useGroupOf(player.registryManager, stack)
        val placeLock = LockGroup.placeGroupOf(player.registryManager, stack)
        val craftLock = LockGroup.craftGroupOf(player.registryManager, stack)

        if (useLock == null && placeLock == null && craftLock == null) return

        if (!keyPressed) {
            tooltip.add(HINT.text)
            return
        }

        useLock?.let { appendRequirements(it, player, tooltip, USE.text) }
        placeLock?.let { appendRequirements(it, player, tooltip, PLACE.text) }
        craftLock?.let { appendRequirements(it, player, tooltip, CRAFT.text) }
    }
}
