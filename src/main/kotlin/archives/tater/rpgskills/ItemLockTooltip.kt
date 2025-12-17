package archives.tater.rpgskills

import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import com.mojang.authlib.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.function.Consumer

object ItemLockTooltip {
    val REQUIRES = Translation.unit("rpgskills.tooltip.stack.requires")
    val REQUIRES_ANY = Translation.unit("rpgskills.tooltip.stack.requires.any")
    val REQUIREMENT = Translation.arg("rpgskills.tooltip.stack.requirement") { formatted(Formatting.DARK_GRAY) }

    @JvmStatic
    @JvmOverloads
    fun appendRequirements(lockGroup: LockGroup, player: PlayerEntity, text: Consumer<Text>, tooltip: Boolean = true) {

        val skills = player[SkillsComponent]

        text.accept((if (lockGroup.requirements.size == 1) REQUIRES.text() else REQUIRES_ANY.text()).apply {
            formatted(when {
                !tooltip -> Formatting.BLACK
                lockGroup.isSatisfiedBy(player) -> Formatting.WHITE
                else -> Formatting.RED
            })
        })

        for (requirement in lockGroup.requirements) {
            text.accept(REQUIREMENT.text(Text.empty().apply {
                requirement.entries.forEachIndexed { index, (skill, level) ->
                    if (index != 0)
                        append(Text.literal(" + ").formatted(Formatting.DARK_GRAY))
                    append(skill.name.apply { if (tooltip) formatted(Formatting.WHITE) })
                    append(Text.literal(" $level").formatted(
                        when {
                            skills[skill] < level -> Formatting.RED
                            tooltip -> Formatting.AQUA
                            else -> Formatting.BLUE
                        }
                    ))
                }
            }))
        }
    }

    fun appendRequirements(lockGroup: LockGroup, player: PlayerEntity, text: MutableList<Text>, tooltip: Boolean = true) {
        appendRequirements(lockGroup, player, text::add, tooltip)
    }

    @JvmStatic
    fun appendTooltip(stack: ItemStack, player: PlayerEntity?, tooltip: MutableList<Text>) {
        val lockGroup = LockGroup.findLocked(player ?: return, stack) ?: return // TODO Show tooltip when unlocked

        appendRequirements(lockGroup, player, tooltip)
    }
}
