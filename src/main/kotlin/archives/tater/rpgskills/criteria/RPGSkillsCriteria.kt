package archives.tater.rpgskills.criteria

import archives.tater.rpgskills.RPGSkills
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.advancement.criterion.ItemCriterion
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.network.ServerPlayerEntity

object RPGSkillsCriteria {
    private fun <T : Criterion<*>> register(path: String, criterion: T): T =
        Registry.register(Registries.CRITERION, RPGSkills.id(path), criterion)

    val BREAK_BLOCK = register("break_block", ItemCriterion())

    fun register() {
        PlayerBlockBreakEvents.BEFORE.register { _, player, pos, _, _ ->
            if (player is ServerPlayerEntity)
                BREAK_BLOCK.trigger(player, pos, player.mainHandStack)
            true
        }
    }
}