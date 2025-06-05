package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.Translation
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ItemLockTooltip {
    val REQUIRES = Translation.unit("rpgskills.tooltip.stack.requires")
    val REQUIRES_ANY = Translation.unit("rpgskills.tooltip.stack.requires.any")
    val REQUIREMENT = Translation.arg("rpgskills.tooltip.stack.requirement") { formatted(Formatting.DARK_GRAY) }

    @JvmStatic
    @JvmOverloads
    fun appendRequirements(lockGroup: LockGroup, text: MutableList<Text>, tooltip: Boolean = true) {
        text.add((if (lockGroup.requirements.size == 1) REQUIRES.text() else { REQUIRES_ANY.text() })
            .formatted(if (tooltip) Formatting.RED else Formatting.BLACK))

        for (requirement in lockGroup.requirements) {
            text.add(REQUIREMENT.text(Text.empty().apply {
                requirement.entries.forEachIndexed { index, (skill, level) ->
                    if (index != 0)
                        append(Text.literal(" + ").formatted(Formatting.DARK_GRAY))
                    append(skill.name.apply { if (tooltip) formatted(Formatting.WHITE) })
                    append(Text.literal(" $level").formatted(if (tooltip) Formatting.GRAY else Formatting.DARK_AQUA))
                }
            }))
        }
    }

    @JvmStatic
    fun appendTooltip(stack: ItemStack, player: PlayerEntity?, tooltip: MutableList<Text>) {
        val lockGroup = LockGroup.findLocked(player ?: return, stack) ?: return

        appendRequirements(lockGroup, tooltip)
    }
}
