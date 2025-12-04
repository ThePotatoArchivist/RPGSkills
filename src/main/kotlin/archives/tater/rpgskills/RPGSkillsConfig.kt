package archives.tater.rpgskills

import archives.tater.rpgskills.data.AnonymousAttributeModifier
import archives.tater.rpgskills.util.CodecConfig
import archives.tater.rpgskills.util.MutationCodec
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.forAccess
import archives.tater.rpgskills.util.isIn
import archives.tater.rpgskills.util.recordMutationCodec
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

class RPGSkillsConfig {
    var chunkSkillPoints: Int = 7
        private set
    var spawnerSkillPoints: Int = 20
        private set
    var structureSkillPoints: Map<TagKey<Structure>, Int> = mapOf(
        RPGSkillsTags.MID_STRUCTURES to 30,
        RPGSkillsTags.HARD_STRUCTURES to 50,
        RPGSkillsTags.BOSS_STRUCTURES to 80,
    )
        private set
    var defaultStructureSkillPoints: Int = 7
        private set
    var entitySkillPoints: Map<TagKey<EntityType<*>>, Int> = mapOf(
        RPGSkillsTags.MINIBOSS to 10,
        RPGSkillsTags.BASIC_BOSS to 30,
        RPGSkillsTags.EARLY_BOSS to 500,
        RPGSkillsTags.MID_BOSS to 800,
        RPGSkillsTags.FINAL_BOSS to 4000,
        RPGSkillsTags.DLC_BOSS to 8000, // TODO tweak value
    )
        private set
    var defaultEntitySkillPointDivisor: Int = 2
        private set
    var blockSkillPointDivisor: Int = 2
        private set
    var advancementSkillPointDivisor: Int = 1
        private set
    var fishingSkillPoints: IntProvider = UniformIntProvider.create(1, 3)
        private set
    var breedingSkillPoints: IntProvider = UniformIntProvider.create(1, 2)
        private set
    var baseLevelCap: Int = 10
        private set
    var levelCapIncreasePerBoss: Int = 10
        private set
    var attributeIncreasesRaw: Map<RegistryKey<EntityAttribute>, AnonymousAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_ATTACK_DAMAGE to AnonymousAttributeModifier(1.0),
        EntityAttributes.GENERIC_MAX_HEALTH to AnonymousAttributeModifier(0.15, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
    ).mapKeys { (id, _) -> id.key.orElseThrow() }
        private set

    val attributeIncreases: Map<RegistryEntry<EntityAttribute>, AnonymousAttributeModifier> by lazy {
        attributeIncreasesRaw.mapKeys { (key, _) -> Registries.ATTRIBUTE.getEntry(key).orElseThrow() }
    }

    fun getStructurePoints(structure: RegistryEntry<Structure>) =
        structureSkillPoints.firstNotNullOfOrNull { (tag, points) -> points.takeIf { structure isIn tag } } ?: defaultStructureSkillPoints

    fun getEntityPoints(entity: LivingEntity) =
        entitySkillPoints.firstNotNullOfOrNull { (tag, points) -> points.takeIf { entity isIn tag } }
            ?: (entity.getXpToDrop(entity.world as ServerWorld, null) ceilDiv defaultEntitySkillPointDivisor)

    fun getBlockPoints(experiencePoints: Int) = experiencePoints ceilDiv blockSkillPointDivisor

    fun getAdvancementPoints(rewards: AdvancementRewards) = rewards.experience ceilDiv advancementSkillPointDivisor

    companion object : CodecConfig<RPGSkillsConfig>(RPGSkills.MOD_ID, RPGSkills.logger) {
        override val codec: MutationCodec<RPGSkillsConfig> = recordMutationCodec(
            Codec.INT.fieldOf("skill_points_chunk").forAccess(RPGSkillsConfig::chunkSkillPoints),
            Codec.INT.fieldOf("skill_points_spawner").forAccess(RPGSkillsConfig::spawnerSkillPoints),
            Codec.unboundedMap(TagKey.unprefixedCodec(RegistryKeys.STRUCTURE), Codec.INT).fieldOf("skill_points_structure").forAccess(RPGSkillsConfig::structureSkillPoints),
            Codec.INT.fieldOf("skill_points_structure_default").forAccess(RPGSkillsConfig::defaultStructureSkillPoints),
            Codec.unboundedMap(TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE), Codec.INT).fieldOf("skill_points_entity").forAccess(RPGSkillsConfig::entitySkillPoints),
            Codec.INT.fieldOf("skill_point_divisor_entity_default").forAccess(RPGSkillsConfig::defaultEntitySkillPointDivisor),
            Codec.INT.fieldOf("skill_point_divisor_block").forAccess(RPGSkillsConfig::blockSkillPointDivisor),
            Codec.INT.fieldOf("skill_point_divisor_advancement").forAccess(RPGSkillsConfig::advancementSkillPointDivisor),
            IntProvider.POSITIVE_CODEC.fieldOf("skill_points_from_fishing").forAccess(RPGSkillsConfig::fishingSkillPoints),
            IntProvider.POSITIVE_CODEC.fieldOf("skill_points_from_breeding").forAccess(RPGSkillsConfig::breedingSkillPoints),
            Codec.INT.fieldOf("level_cap_base").forAccess(RPGSkillsConfig::baseLevelCap),
            Codec.INT.fieldOf("level_cap_increase_per_boss").forAccess(RPGSkillsConfig::levelCapIncreasePerBoss),
            Codec.unboundedMap(RegistryKey.createCodec(RegistryKeys.ATTRIBUTE), AnonymousAttributeModifier.SHORT_CODEC).fieldOf("attribute_increase_per_boss").forAccess(RPGSkillsConfig::attributeIncreasesRaw),
        )

        override fun getDefault() = RPGSkillsConfig()
    }
}