package archives.tater.rpgskills.entity

import archives.tater.rpgskills.RPGSkills
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object RPGSkillsEntities {
    val SKILL_POINT_ORB = Registry.register(
        Registries.ENTITY_TYPE,
        RPGSkills.id("skill_point_orb"),
        EntityType.Builder.create(::SkillPointOrbEntity, SpawnGroup.MISC).apply {
            dimensions(0.5F, 0.5F)
            maxTrackingRange(6)
            trackingTickInterval(20)
        }.build()
    )

    fun register() {}
}