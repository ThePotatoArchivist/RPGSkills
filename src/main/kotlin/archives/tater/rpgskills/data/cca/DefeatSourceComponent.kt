package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.data.SkillSource
import archives.tater.rpgskills.entity.SkillPointOrbEntity
import archives.tater.rpgskills.util.*
import com.mojang.serialization.Codec
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.structure.StructureStart
import net.minecraft.util.Uuids
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import java.util.*

class DefeatSourceComponent(val entity: MobEntity) : Component {
    private var _attackers = mutableMapOf<UUID, Float>()
    val attackers: Map<UUID, Float> get() = _attackers

    var skillSource: SkillSource = SkillSource.EmptySource

    operator fun get(player: PlayerEntity) = attackers.getOrDefault(player.uuid, 0f)
    operator fun set(player: PlayerEntity, value: Float) {
        _attackers[player.uuid] = value
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.update(NbtOps.INSTANCE, tag, this) { it.logIfError() }
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, tag) { it.logIfError() }
    }

    val skillPointProportions get() = attackers.mapValues { (_, damage) ->
        (damage / entity.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH).toFloat())
            .coerceAtMost(1f)
    }

    fun getSkillPointAmounts(): Map<UUID, Int> {
        val source = skillSource.getComponent(entity.world) ?: return mapOf()
        return skillPointProportions.mapValues { (uuid, proportion) ->
            source.removeSkillPoints(uuid, (proportion * getMaxSkillPoints(entity)).toInt())
        }
    }

    companion object : ComponentKeyHolder<DefeatSourceComponent, MobEntity> {
        val CODEC = recordMutationCodec(
            Codec.unboundedMap(Uuids.STRING_CODEC, Codec.FLOAT).mutate().fieldFor("attackers", DefeatSourceComponent::_attackers),
            SkillSource.CODEC.fieldOf("skill_source").forAccess(DefeatSourceComponent::skillSource),
        )

        override val key: ComponentKey<DefeatSourceComponent> = ComponentRegistry.getOrCreate(RPGSkills.id("defeat_source"), DefeatSourceComponent::class.java)

        @JvmField
        val KEY = key

        fun getMaxSkillPoints(entity: MobEntity): Int =
            entity.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH).toInt() / 5 // TODO decide skill points formula

        @JvmStatic
        fun onSpawn(entity: MobEntity) {
            val component = entity[DefeatSourceComponent]
            if (component.skillSource != SkillSource.EmptySource) return

            val structure = (entity.world as ServerWorld).structureAccessor.getStructureContaining(entity.blockPos, RPGSkillsTags.HAS_SKILL_POOL_STRUCTURE)
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
            baseDamageTaken: Float,
            damageTaken: Float,
            blocked: Boolean
        ) {
            if (entity !is MobEntity) return
            val player = source.attacker as? PlayerEntity ?: return
            entity[DefeatSourceComponent][player] += damageTaken

            if (!entity.isDead) return

            val world = entity.world as? ServerWorld ?: return
            for ((playerUuid, amount) in entity[DefeatSourceComponent].getSkillPointAmounts())
                SkillPointOrbEntity.spawnOrbs(world, world.getPlayerByUuid(playerUuid) ?: continue, entity.pos, amount)
        }
    }
}