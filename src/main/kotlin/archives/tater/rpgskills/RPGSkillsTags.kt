package archives.tater.rpgskills

import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object RPGSkillsTags {
    private fun <T> of(registry: RegistryKey<Registry<T>>, path: String): TagKey<T> = TagKey.of(registry, RPGSkills.id(path))

    private fun ofEntity(path: String) = of(RegistryKeys.ENTITY_TYPE, path)
    private fun ofStructure(path: String) = of(RegistryKeys.STRUCTURE, path)

    val MINIBOSS = ofEntity("bosses/miniboss")
    val BASIC_BOSS = ofEntity("bosses/basic")
    val EARLY_BOSS = ofEntity("bosses/early")
    val MID_BOSS = ofEntity("bosses/mid")
    val FINAL_BOSS = ofEntity("bosses/final")
    val DLC_BOSS = ofEntity("bosses/dlc")
    val IGNORES_SKILL_SOURCE = ofEntity("ignores_skill_source")

    val MID_STRUCTURES = ofStructure("skill/mid")
    val HARD_STRUCTURES = ofStructure("skill/hard")
    val BOSS_STRUCTURES = ofStructure("skill/boss")
}
