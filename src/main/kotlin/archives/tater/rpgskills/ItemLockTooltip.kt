package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.Translation
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ItemLockTooltip {
    val REQUIRES = Translation.unit("rpgskills.tooltip.stack.requires") { formatted(Formatting.RED) }
    val REQUIRES_ANY = Translation.unit("rpgskills.tooltip.stack.requires.any") { formatted(Formatting.RED) }
    val REQUIREMENT = Translation.arg("rpgskills.tooltip.stack.requirement") { formatted(Formatting.DARK_GRAY) }

    @JvmStatic
    fun appendTooltip(stack: ItemStack, player: PlayerEntity?, tooltip: MutableList<Text>) {
        val lockGroup = LockGroup.findLocked(player ?: return, stack) ?: return

        tooltip.add(if (lockGroup.requirements.size == 1) REQUIRES.text else { REQUIRES_ANY.text })

        for (requirement in lockGroup.requirements) {
            tooltip.add(REQUIREMENT.text(Text.empty().apply {
                requirement.entries.forEachIndexed { index, (skill, level) ->
                    if (index != 0)
                        append(Text.literal(" + ").formatted(Formatting.DARK_GRAY))
                    append(skill.name.formatted(Formatting.WHITE))
                    append(Text.literal(" $level").formatted(Formatting.GRAY))
                }
            }))
        }
    }
}
