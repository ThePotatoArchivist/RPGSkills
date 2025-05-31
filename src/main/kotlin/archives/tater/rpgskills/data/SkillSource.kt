package archives.tater.rpgskills.data

import archives.tater.rpgskills.data.cca.SkillSourceComponent
import archives.tater.rpgskills.data.cca.StructuresSkillSourceComponent
import archives.tater.rpgskills.data.cca.skillSource
import archives.tater.rpgskills.util.field
import archives.tater.rpgskills.util.get
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.KeyDispatchCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.gen.structure.Structure
import kotlin.jvm.optionals.getOrNull

sealed class SkillSource {
    abstract fun getComponent(world: World): SkillSourceComponent?

    data class ChunkSource(val pos: ChunkPos) : SkillSource() {
        inline val x get() = pos.x
        inline val z get() = pos.z

        override fun getComponent(world: World): SkillSourceComponent =
            world.getChunk(pos.x, pos.z).skillSource

        constructor(x: Int, z: Int) : this(ChunkPos(x, z))

        companion object {
            val CODEC: MapCodec<ChunkSource> = RecordCodecBuilder.mapCodec { it.group(
                field("x", ChunkSource::x, Codec.INT),
                field("z", ChunkSource::z, Codec.INT),
            ).apply(it, ::ChunkSource) }
        }
    }

    data class SpawnerSource(val pos: BlockPos): SkillSource() {
        override fun getComponent(world: World): SkillSourceComponent? =
            world.getBlockEntity(pos, BlockEntityType.MOB_SPAWNER).getOrNull()?.skillSource

        companion object {
            val CODEC: MapCodec<SpawnerSource> = RecordCodecBuilder.mapCodec { it.group(
                field("pos", SpawnerSource::pos, BlockPos.CODEC)
            ).apply(it, ::SpawnerSource) }
        }
    }

    data class StructureSource(val box: BlockBox, val structure: RegistryKey<Structure>) : SkillSource() {
        override fun getComponent(world: World): SkillSourceComponent =
            world[StructuresSkillSourceComponent].getOrCreate(box, structure)

        companion object {
            val CODEC: MapCodec<StructureSource> = RecordCodecBuilder.mapCodec {
                it.group(
                    field("box", StructureSource::box, BlockBox.CODEC),
                    field("structure", StructureSource::structure, RegistryKey.createCodec(RegistryKeys.STRUCTURE)),
                ).apply(it, ::StructureSource)
            }
        }
    }

    // TODO empty

    companion object {
        val CODEC: Codec<SkillSource> = KeyDispatchCodec(
            "type", Codec.STRING,
            { when (it) {
                    is ChunkSource -> DataResult.success("chunk")
                    is SpawnerSource -> DataResult.success("spawner")
                    is StructureSource -> DataResult.success("structure")
                    else -> DataResult.error { "Unknown source ${it::class.simpleName}" }
            } },
            { when (it) {
                "chunk" -> DataResult.success(ChunkSource.CODEC)
                "spawner" -> DataResult.success(SpawnerSource.CODEC)
                "structure" -> DataResult.success(StructureSource.CODEC)
                else -> DataResult.error { "Unknown type $it" }
            } },
        ).codec()
    }
}