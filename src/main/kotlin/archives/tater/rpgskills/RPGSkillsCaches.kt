package archives.tater.rpgskills

import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.RegistryCache
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback

object RPGSkillsCaches {
    val JOB_TO_SKILL = RegistryCache(Skill.key) { skill -> skill.levels.flatMap { level -> level.jobs.map { it.key.orElseThrow() } } }

//    val ITEM_TO_LOCKGROUP = RegistryCache(LockGroup.key) { it.items.entries.matchingValues }

    fun register() {
        DynamicRegistrySetupCallback.EVENT.register(JOB_TO_SKILL)
    }
}