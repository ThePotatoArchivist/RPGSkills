package archives.tater.rpgskills

import archives.tater.rpgskills.data.SkillsComponent
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer

@Suppress("UnstableApiUsage")
object RPGSkillsComponents : EntityComponentInitializer {
    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(SkillsComponent.key, ::SkillsComponent)
    }
}
