package archives.tater.rpgskills.entity

import archives.tater.rpgskills.RPGSkills
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.EntityType.EntityFactory
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object RPGSkillsEntities {
    private fun <T: Entity> register(path: String, type: EntityType<T>): EntityType<T> =
        Registry.register(Registries.ENTITY_TYPE, RPGSkills.id(path), type)

    private fun <T: Entity> register(path: String, factory: EntityFactory<T>, spawnGroup: SpawnGroup = SpawnGroup.MISC, init: EntityType.Builder<T>.() -> Unit) =
        register(path, EntityType.Builder.create(factory, spawnGroup).apply(init).build())

    val SKILL_POINT_ORB: EntityType<SkillPointOrbEntity> = register("skill_point_orb", ::SkillPointOrbEntity) {
        dimensions(0.5F, 0.5F)
        maxTrackingRange(6)
        trackingTickInterval(20)
    }

    fun register() {}
}