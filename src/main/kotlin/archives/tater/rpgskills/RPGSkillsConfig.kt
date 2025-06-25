package archives.tater.rpgskills

import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.CodecConfig
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.isIn
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.gen.structure.Structure

data class RPGSkillsConfig(
    val chunkSkillPoints: Int = 7,
    val spawnerSkillPoints: Int = 20,
    val structureSkillPoints: Map<TagKey<Structure>, Int> = mapOf(
        RPGSkillsTags.MID_STRUCTURES to 30,
        RPGSkillsTags.HARD_STRUCTURES to 50,
        RPGSkillsTags.BOSS_STRUCTURES to 80,
    ),
    val defaultStructureSkillPoints: Int = 7,
    val entitySkillPoints: Map<TagKey<EntityType<*>>, Int> = mapOf(
        RPGSkillsTags.MINIBOSS to 10,
        RPGSkillsTags.BASIC_BOSS to 30,
        RPGSkillsTags.EARLY_BOSS to 500,
        RPGSkillsTags.MID_BOSS to 800,
        RPGSkillsTags.FINAL_BOSS to 4000,
        RPGSkillsTags.DLC_BOSS to SkillsComponent.MAX_POINTS,
    ),
    val defaultEntitySkillPointDivisor: Int = 2
) {

    fun getStructurePoints(structure: RegistryEntry<Structure>) =
        structureSkillPoints.firstNotNullOfOrNull { (tag, points) -> points.takeIf { structure isIn tag } } ?: 0

    fun getEntityPoints(entity: LivingEntity) =
        entitySkillPoints.firstNotNullOfOrNull { (tag, points) -> points.takeIf { entity isIn tag } }
            ?: (entity.getXpToDrop(entity.world as ServerWorld, null) ceilDiv defaultEntitySkillPointDivisor)

    companion object : CodecConfig<RPGSkillsConfig>(RPGSkills.MOD_ID, RPGSkills.logger) {
        override val codec: Codec<RPGSkillsConfig> = RecordCodecBuilder.create { it.group(
            Codec.INT.fieldOf("chunk_skill_points").forGetter(RPGSkillsConfig::chunkSkillPoints),
            Codec.INT.fieldOf("spawner_skill_points").forGetter(RPGSkillsConfig::spawnerSkillPoints),
            Codec.unboundedMap(TagKey.unprefixedCodec(RegistryKeys.STRUCTURE), Codec.INT).fieldOf("structure_skill_points").forGetter(RPGSkillsConfig::structureSkillPoints),
            Codec.INT.fieldOf("default_structure_skill_points").forGetter(RPGSkillsConfig::defaultStructureSkillPoints),
            Codec.unboundedMap(TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE), Codec.INT).fieldOf("entity_skill_points").forGetter(RPGSkillsConfig::entitySkillPoints),
            Codec.INT.fieldOf("default_entity_skill_point_divisor").forGetter(RPGSkillsConfig::defaultEntitySkillPointDivisor),
        ).apply(it, ::RPGSkillsConfig) }

        override val defaultConfig = RPGSkillsConfig()
    }
}