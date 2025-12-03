package archives.tater.rpgskills

import archives.tater.rpgskills.data.AnonymousAttributeModifier
import archives.tater.rpgskills.util.CodecConfig
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.isIn
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.AdvancementRewards
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.intprovider.IntProvider
import net.minecraft.util.math.intprovider.UniformIntProvider
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
        RPGSkillsTags.DLC_BOSS to 8000, // TODO tweak value
    ),
    val defaultEntitySkillPointDivisor: Int = 2,
    val blockSkillPointDivisor: Int = 2,
    val advancementSkillPointDivisor: Int = 1,
    val fishingSkillPoints: IntProvider = UniformIntProvider.create(1, 3),
    val breedingSkillPoints: IntProvider = UniformIntProvider.create(1, 2),
    val baseLevelCap: Int = 10,
    val levelCapIncreasePerBoss: Int = 10,
    val attributeIncreasesRaw: Map<RegistryKey<EntityAttribute>, AnonymousAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_ATTACK_DAMAGE to AnonymousAttributeModifier(1.0),
        EntityAttributes.GENERIC_MAX_HEALTH to AnonymousAttributeModifier(0.15, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
    ).mapKeys { (id, _) -> id.key.orElseThrow() },
) {

    val attributeIncreases: Map<RegistryEntry<EntityAttribute>, AnonymousAttributeModifier> by lazy {
        attributeIncreasesRaw.mapKeys { (key, _) -> Registries.ATTRIBUTE.getEntry(key).orElseThrow() }
    }

    fun getStructurePoints(structure: RegistryEntry<Structure>) =
        structureSkillPoints.firstNotNullOfOrNull { (tag, points) -> points.takeIf { structure isIn tag } } ?: 0

    fun getEntityPoints(entity: LivingEntity) =
        entitySkillPoints.firstNotNullOfOrNull { (tag, points) -> points.takeIf { entity isIn tag } }
            ?: (entity.getXpToDrop(entity.world as ServerWorld, null) ceilDiv defaultEntitySkillPointDivisor)

    fun getBlockPoints(experiencePoints: Int) = experiencePoints ceilDiv blockSkillPointDivisor

    fun getAdvancementPoints(rewards: AdvancementRewards) = rewards.experience ceilDiv advancementSkillPointDivisor

    companion object : CodecConfig<RPGSkillsConfig>(RPGSkills.MOD_ID, RPGSkills.logger) {
        override val codec: Codec<RPGSkillsConfig> = RecordCodecBuilder.create { it.group(
            Codec.INT.fieldOf("skill_points_chunk").forGetter(RPGSkillsConfig::chunkSkillPoints),
            Codec.INT.fieldOf("skill_points_spawner").forGetter(RPGSkillsConfig::spawnerSkillPoints),
            Codec.unboundedMap(TagKey.unprefixedCodec(RegistryKeys.STRUCTURE), Codec.INT).fieldOf("skill_points_structure").forGetter(RPGSkillsConfig::structureSkillPoints),
            Codec.INT.fieldOf("skill_points_structure_default").forGetter(RPGSkillsConfig::defaultStructureSkillPoints),
            Codec.unboundedMap(TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE), Codec.INT).fieldOf("skill_points_entity").forGetter(RPGSkillsConfig::entitySkillPoints),
            Codec.INT.fieldOf("skill_point_divisor_entity_default").forGetter(RPGSkillsConfig::defaultEntitySkillPointDivisor),
            Codec.INT.fieldOf("skill_point_divisor_block").forGetter(RPGSkillsConfig::blockSkillPointDivisor),
            Codec.INT.fieldOf("skill_point_divisor_advancement").forGetter(RPGSkillsConfig::advancementSkillPointDivisor),
            IntProvider.POSITIVE_CODEC.fieldOf("skill_points_from_fishing").forGetter(RPGSkillsConfig::fishingSkillPoints),
            IntProvider.POSITIVE_CODEC.fieldOf("skill_points_from_breeding").forGetter(RPGSkillsConfig::breedingSkillPoints),
            Codec.INT.fieldOf("level_cap_base").forGetter(RPGSkillsConfig::baseLevelCap),
            Codec.INT.fieldOf("level_cap_increase_per_boss").forGetter(RPGSkillsConfig::levelCapIncreasePerBoss),
            Codec.unboundedMap(RegistryKey.createCodec(RegistryKeys.ATTRIBUTE), AnonymousAttributeModifier.SHORT_CODEC).fieldOf("attribute_increase_per_boss").forGetter(RPGSkillsConfig::attributeIncreasesRaw),
        ).apply(it, ::RPGSkillsConfig) }

        override val defaultConfig = RPGSkillsConfig()
    }
}