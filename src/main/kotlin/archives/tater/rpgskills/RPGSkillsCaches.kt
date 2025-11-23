package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.RegistryCache
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback

object RPGSkillsCaches {
    val JOB_TO_SKILL = RegistryCache(Skill.key) { skill -> skill.value.levels.flatMap { level -> level.jobs } }

    val ITEM_TO_LOCKGROUP = RegistryCache(LockGroup.key) { it.value.items.entries.matchingValues }
    val BLOCK_TO_LOCKGROUP = RegistryCache(LockGroup.key) { it.value.blocks.entries.matchingValues }
    val ENTITY_TO_LOCKGROUP = RegistryCache(LockGroup.key) { it.value.entities.entries.matchingValues }
    val RECIPE_TO_LOCKGROUP = RegistryCache(LockGroup.key) { it.value.recipes.entries }
}