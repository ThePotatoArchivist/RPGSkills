package archives.tater.rpgskills

import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object RPGSkillsTags {
    private fun <T> of(registry: RegistryKey<Registry<T>>, path: String) = TagKey.of(registry, RPGSkills.id(path))

    val HAS_SKILL_POOL_STRUCTURE = of(RegistryKeys.STRUCTURE, "has_skill_pool")
}