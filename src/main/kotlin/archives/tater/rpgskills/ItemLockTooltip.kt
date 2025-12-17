package archives.tater.rpgskills

import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import com.mojang.authlib.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.function.Consumer
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEachIndexed

object ItemLockTooltip {
    val REQUIRES = Translation.unit("rpgskills.tooltip.stack.requires")
    val REQUIRES_ANY = Translation.unit("rpgskills.tooltip.stack.requires.any")
    val REQUIREMENT = Translation.arg("rpgskills.tooltip.stack.requirement") { formatted(Formatting.DARK_GRAY) }

    @JvmStatic
    fun getRequirement(requirement: Collection<Map.Entry<RegistryEntry<Skill>, Int>>, player: PlayerEntity, tooltip: Boolean = true): Text = Text.empty().apply {

        val skills = player[SkillsComponent]

        requirement.forEachIndexed { index, (skill, level) ->
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
    }

    fun getTitle(lockGroup: LockGroup, player: PlayerEntity, tooltip: Boolean): Text =
        (if (lockGroup.requirements.size == 1) REQUIRES.text() else REQUIRES_ANY.text()).apply {
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
    fun appendRequirements(lockGroup: LockGroup, player: PlayerEntity, text: Consumer<Text>, tooltip: Boolean = true) {
        text.accept(getTitle(lockGroup, player, tooltip))
        for (requirement in lockGroup.requirements)
            text.accept(REQUIREMENT.text(getRequirement(requirement.entries, player, tooltip)))
    }

    fun appendRequirements(lockGroup: LockGroup, player: PlayerEntity, text: MutableList<Text>, tooltip: Boolean = true) {
        appendRequirements(lockGroup, player, text::add, tooltip)
    }

    @JvmStatic
    fun appendTooltip(stack: ItemStack, player: PlayerEntity, tooltip: MutableList<Text>) {
        val lockGroup = LockGroup.groupOf(player.registryManager, stack) ?: return

        appendRequirements(lockGroup, player, tooltip)
    }
}
