package archives.tater.rpgskills.data.cca

import net.minecraft.entity.mob.MobEntity
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer

@Suppress("UnstableApiUsage")
object RPGSkillsComponents : EntityComponentInitializer {
    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(SkillsComponent.key, ::SkillsComponent)
        registry.registerFor(MobEntity::class.java, DefeatSourceComponent.key, ::DefeatSourceComponent)
    }
}
