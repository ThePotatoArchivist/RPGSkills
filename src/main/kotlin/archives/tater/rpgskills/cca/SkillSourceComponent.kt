package archives.tater.rpgskills.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.*
import com.mojang.serialization.Codec
import net.minecraft.block.entity.MobSpawnerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Uuids
import net.minecraft.world.chunk.Chunk
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import java.util.*

class SkillSourceComponent(private val initialPoints: Int, private val markDirty: (() -> Unit)? = null) : Component {
    private val remainingSkillPoints = mutableMapOf<UUID, Int>()

    operator fun get(playerUuid: UUID) = remainingSkillPoints.getOrDefault(playerUuid, initialPoints)
    operator fun set(playerUuid: UUID, points: Int) {
        remainingSkillPoints[playerUuid] = points
        markDirty?.invoke()
    }

    operator fun get(player: PlayerEntity) = this[player.uuid]
    operator fun set(player: PlayerEntity, points: Int) {
        this[player.uuid] = points
    }

    fun removeSkillPoints(player: PlayerEntity, max: Int) = removeSkillPoints(player.uuid, max)

    fun removeSkillPoints(playerUuid: UUID, max: Int) =
        get(playerUuid).coerceAtMost(max).also {
            this[playerUuid] -= it
        }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.update(this, tag).logIfError()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, tag).logIfError().ifError {
            markDirty?.invoke() // So that it saves and doesn't throw an error next time
        }
    }

    companion object {
        val CODEC = recordMutationCodec(
            Codec.unboundedMap(Uuids.STRING_CODEC, Codec.INT).mutate().fieldFor("remaining_points", SkillSourceComponent::remainingSkillPoints)
        )

        fun createCodec(initialPoints: Int = 0) = CODEC.codec { SkillSourceComponent(initialPoints) }

        val KEY: ComponentKey<SkillSourceComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("skill_source"), SkillSourceComponent::class.java)
    }
}

val MobSpawnerBlockEntity.skillSource by SkillSourceComponent.KEY
val Chunk.skillSource by SkillSourceComponent.KEY
