package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.data.SkillPointConstants
import net.minecraft.block.entity.MobSpawnerBlockEntity
import net.minecraft.entity.mob.MobEntity
import org.ladysnake.cca.api.v3.block.BlockComponentFactoryRegistry
import org.ladysnake.cca.api.v3.block.BlockComponentInitializer
import org.ladysnake.cca.api.v3.chunk.ChunkComponentFactoryRegistry
import org.ladysnake.cca.api.v3.chunk.ChunkComponentInitializer
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer

@Suppress("UnstableApiUsage")
object RPGSkillsComponents : EntityComponentInitializer, ChunkComponentInitializer, WorldComponentInitializer, BlockComponentInitializer {
    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(SkillsComponent.key, ::SkillsComponent)
        registry.registerFor(MobEntity::class.java, DefeatSourceComponent.key, ::DefeatSourceComponent)
    }

    override fun registerChunkComponentFactories(registry: ChunkComponentFactoryRegistry) {
        registry.register(SkillSourceComponent.KEY) {
            SkillSourceComponent(SkillPointConstants.CHUNK_SKILL_POINTS) { it.setNeedsSaving(true) }
        }
    }

    override fun registerWorldComponentFactories(registry: WorldComponentFactoryRegistry) {
        registry.register(StructuresSkillSourceComponent.KEY, ::StructuresSkillSourceComponent)
    }

    override fun registerBlockComponentFactories(registry: BlockComponentFactoryRegistry) {
        registry.registerFor(MobSpawnerBlockEntity::class.java, SkillSourceComponent.KEY) {
            SkillSourceComponent(SkillPointConstants.SPAWNER_SKILL_POINTS) { it.markDirty() }
        }
    }
}
