package archives.tater.rpgskills.data

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.registry.RegistryKey
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureKeys

object SkillPointConstants {
    const val CHUNK_SKILL_POINTS = 20
    const val SPAWNER_SKILL_POINTS = 100

    /**
     * See [StructureTagGenerator.configure][archives.tater.rpgskills.datagen.StructureTagGenerator.configure] to configure which structures have their own skill pool
     */
    fun getStructurePoints(structure: RegistryKey<Structure>) = when(structure) {
        StructureKeys.MANSION -> 100
        else -> 40
    }

    fun getEntityPoints(entity: LivingEntity)  =
        entity.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH).toInt() / 5
}