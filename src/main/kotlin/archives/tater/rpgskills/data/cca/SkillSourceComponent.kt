package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.forAccess
import archives.tater.rpgskills.util.getValue
import archives.tater.rpgskills.util.recordMutationCodec
import com.mojang.serialization.Codec
import net.minecraft.block.entity.MobSpawnerBlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryWrapper
import net.minecraft.world.chunk.Chunk
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry

class SkillSourceComponent(initialPoints: Int, private val onUpdate: (() -> Unit)? = null) : Component {
    var remainingSkillPoints = initialPoints
        set(value) {
            field = value
            onUpdate?.invoke()
        }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.update(NbtOps.INSTANCE, tag, this)
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, NbtOps.INSTANCE, tag)
    }

    companion object {
        val CODEC = recordMutationCodec(
            Codec.INT.fieldOf("remaining_points").forAccess(SkillSourceComponent::remainingSkillPoints)
        )

        fun createCodec(initialPoints: Int = 0) = CODEC.codec { SkillSourceComponent(initialPoints) }

        val KEY: ComponentKey<SkillSourceComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("skill_source"), SkillSourceComponent::class.java)
    }
}

val MobSpawnerBlockEntity.skillSource by SkillSourceComponent.KEY
val Chunk.skillSource by SkillSourceComponent.KEY
