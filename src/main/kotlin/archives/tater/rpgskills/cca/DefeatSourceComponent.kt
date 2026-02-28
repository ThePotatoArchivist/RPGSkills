package archives.tater.rpgskills.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsConfig
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.data.SkillSource
import archives.tater.rpgskills.entity.SkillPointOrbEntity
import archives.tater.rpgskills.util.*
import archives.tater.rpgskills.util.get
import com.mojang.serialization.Codec
import net.minecraft.command.argument.UuidArgumentType.uuid
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.TargetPredicate
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.Uuids
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkPos
import net.spell_engine.api.datagen.SpellBuilder.Impacts.damage
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import java.util.*
import kotlin.collections.iterator

class DefeatSourceComponent(val entity: MobEntity) : Component {
    private var _attackers = mutableMapOf<UUID, Float>()
    val attackers: Map<UUID, Float> get() = _attackers

    var skillSource: SkillSource = SkillSource.EmptySource

    fun getRewarded() = if (entity isIn RPGSkillsTags.PROXIMITY_DEFEAT) {
        RPGSkills.CONFIG.getPlayersInProximity(entity).map { it }
    } else {
        attackers.keys.mapNotNull { entity.world.server?.playerManager?.getPlayer(it) }
    }

    operator fun get(player: PlayerEntity) = attackers.getOrDefault(player.uuid, 0f)
    operator fun set(player: PlayerEntity, value: Float) {
        _attackers[player.uuid] = value
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.update(this, tag).logIfError()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, tag).logIfError()
    }

    fun getSkillPointProportions(): Map<out PlayerEntity, Float> = if (entity isIn RPGSkillsTags.PROXIMITY_DEFEAT)
        RPGSkills.CONFIG.getPlayersInProximity(entity).associateWith { 1f }
    else
        attackers.entries.associateNotNull { (uuid, damage) ->
            entity.world.getPlayerByUuid(uuid)?.to((damage / entity.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH).toFloat())
                .coerceAtMost(1f))
        }

    fun getSkillPointAmounts(ignoreCustom: Boolean): Map<PlayerEntity, Int> {
        val source = if (entity isIn RPGSkillsTags.IGNORES_SKILL_SOURCE)
            null
        else
            skillSource.getComponent(entity.world) ?: return mapOf()

        return getSkillPointProportions().mapValues { (player, proportion) ->
            val points = (proportion * RPGSkills.CONFIG.getEntityPoints(entity, ignoreCustom)).toInt()
            source?.removeSkillPoints(player, points) ?: points
        }
    }

    companion object : ComponentKeyHolder<DefeatSourceComponent, MobEntity> {
        val CODEC = recordMutationCodec(
            Codec.unboundedMap(Uuids.STRING_CODEC, Codec.FLOAT).mutate().fieldFor("attackers", DefeatSourceComponent::_attackers),
            SkillSource.CODEC.fieldOf("skill_source").forAccess(DefeatSourceComponent::skillSource),
        )

        override val key: ComponentKey<DefeatSourceComponent> = ComponentRegistry.getOrCreate(RPGSkills.id("defeat_source"), DefeatSourceComponent::class.java)

        @JvmStatic
        fun onSpawn(entity: MobEntity) {
            val component = entity[DefeatSourceComponent]
            if (component.skillSource != SkillSource.EmptySource) return

            val structure = (entity.world as ServerWorld).structureAccessor.getStructureContaining(entity.blockPos) { true }
            component.skillSource =
                if (structure != StructureStart.DEFAULT)
                    SkillSource.StructureSource(structure.boundingBox, entity.registryManager[RegistryKeys.STRUCTURE].getKey(structure.structure).orElseThrow())
                else
                    SkillSource.ChunkSource(ChunkPos(entity.blockPos))
        }

        @JvmStatic
        fun onSpawnFromSpawner(entity: MobEntity, pos: BlockPos) {
            entity[DefeatSourceComponent].skillSource = SkillSource.SpawnerSource(pos)
        }

        @JvmStatic
        fun afterDamage(
            entity: LivingEntity,
            source: DamageSource,
            damageTaken: Float
        ) {
            if (damageTaken == 0f || entity !is MobEntity) return
            val player = source.attacker as? PlayerEntity ?: return
            entity[DefeatSourceComponent][player] += damageTaken

            if (!entity.isDead) return

            val world = entity.world as? ServerWorld ?: return
            for ((player, amount) in entity[DefeatSourceComponent].getSkillPointAmounts(
                entity isIn RPGSkillsTags.REPEATED_DEFEAT_IGNORES_CUSTOM_SKILL_DROP && !world[BossTrackerComponent].hasDefeated(entity)
            ))
                SkillPointOrbEntity.spawnOrbs(world, player, entity.pos, amount)
        }
    }
}
