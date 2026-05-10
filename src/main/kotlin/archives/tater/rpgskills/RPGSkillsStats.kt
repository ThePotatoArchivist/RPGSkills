package archives.tater.rpgskills

import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object RPGSkillsStats {
    private fun register(id: Identifier): Identifier = Registry.register(Registries.CUSTOM_STAT, id, id)
    private fun register(path: String) = register(RPGSkills.id(path))

    @JvmField
    val XP_POINTS_COLLECTED = register("xp_points_collected")

    fun register() {}
}