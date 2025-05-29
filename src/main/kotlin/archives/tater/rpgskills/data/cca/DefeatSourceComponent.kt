package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.entity.SkillPointOrbEntity
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.get
import com.mojang.serialization.Codec
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Uuids
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import java.util.*
import kotlin.jvm.optionals.getOrNull

class DefeatSourceComponent(val entity: MobEntity) : Component {
    private var _attackers = mutableMapOf<UUID, Float>()
    val attackers: Map<UUID, Float> get() = _attackers

    operator fun get(player: PlayerEntity) = attackers.getOrDefault(player.uuid, 0f)
    operator fun set(player: PlayerEntity, value: Float) {
        _attackers[player.uuid] = value
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        _attackers = ATTACKERS_CODEC.decode(NbtOps.INSTANCE, tag.getCompound(ATTACKERS_NBT)).ifError {
            RPGSkills.logger.error("Could not deserialize DefeatSource component: {}", it.message())
        }.result().getOrNull()?.first?.toMutableMap() ?: mutableMapOf()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        ATTACKERS_CODEC.encodeStart(NbtOps.INSTANCE, _attackers).ifSuccess {
            tag.put(ATTACKERS_NBT, it)
        }.ifError {
            RPGSkills.logger.error("Could not serialize DefeatSource component: {}", it.message())
        }
    }

    val skillPointProportions get() = attackers.mapValues { (_, damage) ->
        (damage / entity.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH).toFloat())
            .coerceAtMost(1f)
    }

    val skillPointAmounts get() =
        skillPointProportions.mapValues { (_, proportion) ->
            (proportion * getMaxSkillPoints(entity)).toInt()
        }

    companion object : ComponentKeyHolder<DefeatSourceComponent, MobEntity>, ServerLivingEntityEvents.AfterDamage {
        val ATTACKERS_CODEC: Codec<Map<UUID, Float>> = Codec.unboundedMap(Uuids.STRING_CODEC, Codec.FLOAT)
        const val ATTACKERS_NBT = "attackers"

        override val key: ComponentKey<DefeatSourceComponent> = ComponentRegistry.getOrCreate(RPGSkills.id("defeat_source"), DefeatSourceComponent::class.java)

        @JvmField
        val KEY = key

        fun getMaxSkillPoints(entity: MobEntity): Int =
            entity.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH).toInt() / 5 // TODO decide skill points formula

        @JvmStatic
        override fun afterDamage(
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
            for ((playerUuid, amount) in entity[DefeatSourceComponent].skillPointAmounts)
                SkillPointOrbEntity.spawnOrbs(world, world.getPlayerByUuid(playerUuid) ?: continue, entity.pos, amount)
        }
    }
}