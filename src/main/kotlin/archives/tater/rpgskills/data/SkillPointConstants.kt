package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.isIn
import net.minecraft.entity.LivingEntity
import net.minecraft.registry.RegistryKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureKeys

// TODO make this a config file
object SkillPointConstants {
    const val CHUNK_SKILL_POINTS = 7
    const val SPAWNER_SKILL_POINTS = 20

    fun getStructurePoints(structure: RegistryKey<Structure>) = when(structure) {
        StructureKeys.FORTRESS, StructureKeys.END_CITY, StructureKeys.BASTION_REMNANT -> 30
        StructureKeys.MONUMENT, StructureKeys.MANSION, StructureKeys.TRIAL_CHAMBERS -> 50
        else -> 7
    }

    fun getEntityPoints(entity: LivingEntity) = when {
        entity isIn RPGSkillsTags.MINIBOSS -> 10
        entity isIn RPGSkillsTags.BASIC_BOSS -> 30
        entity isIn RPGSkillsTags.EARLY_BOSS -> 500
        entity isIn RPGSkillsTags.MID_BOSS -> 800
        entity isIn RPGSkillsTags.FINAL_BOSS -> 4000
        entity isIn RPGSkillsTags.DLC_BOSS -> SkillsComponent.MAX_POINTS
        else -> entity.getXpToDrop(entity.world as ServerWorld, null) ceilDiv 2
    }
}