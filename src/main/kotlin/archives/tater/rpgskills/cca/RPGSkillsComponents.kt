package archives.tater.rpgskills.cca

import archives.tater.rpgskills.RPGSkills
import net.minecraft.block.entity.MobSpawnerBlockEntity
import net.minecraft.entity.mob.MobEntity
import org.ladysnake.cca.api.v3.block.BlockComponentFactoryRegistry
import org.ladysnake.cca.api.v3.block.BlockComponentInitializer
import org.ladysnake.cca.api.v3.chunk.ChunkComponentFactoryRegistry
import org.ladysnake.cca.api.v3.chunk.ChunkComponentInitializer
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry
import org.ladysnake.cca.api.v3.scoreboard.ScoreboardComponentInitializer
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer

@Suppress("UnstableApiUsage")
object RPGSkillsComponents : EntityComponentInitializer, ChunkComponentInitializer, WorldComponentInitializer, ScoreboardComponentInitializer, BlockComponentInitializer {
    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(SkillsComponent.key, ::SkillsComponent)
        registry.registerForPlayers(JobsComponent.key, ::JobsComponent)
        registry.registerFor(MobEntity::class.java, DefeatSourceComponent.key, ::DefeatSourceComponent)
    }

    override fun registerChunkComponentFactories(registry: ChunkComponentFactoryRegistry) {
        registry.register(SkillSourceComponent.key) {
            SkillSourceComponent(RPGSkills.CONFIG.chunkSkillPoints) { it.setNeedsSaving(true) }
        }
    }

    override fun registerWorldComponentFactories(registry: WorldComponentFactoryRegistry) {
        registry.register(StructuresSkillSourceComponent.key, ::StructuresSkillSourceComponent)
    }

    override fun registerScoreboardComponentFactories(registry: ScoreboardComponentFactoryRegistry) {
        registry.registerScoreboardComponent(BossTrackerComponent.key, ::BossTrackerComponent)
        registry.registerTeamComponent(BossTrackerComponent.key, ::BossTrackerComponent)
    }

    override fun registerBlockComponentFactories(registry: BlockComponentFactoryRegistry) {
        registry.registerFor(MobSpawnerBlockEntity::class.java, SkillSourceComponent.key) {
            SkillSourceComponent(RPGSkills.CONFIG.spawnerSkillPoints) { it.markDirty() }
        }
    }
}
