package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.util.RegistryKeyHolder
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object RPGSkillsTags {
    private fun <T> of(registry: RegistryKey<Registry<T>>, path: String): TagKey<T> = TagKey.of(registry, RPGSkills.id(path))

    private fun <T> of(registry: RegistryKeyHolder<Registry<T>>, path: String) = of(registry.key, path)

    private fun ofItem(path: String) = of(RegistryKeys.ITEM, path)
    private fun ofEntity(path: String) = of(RegistryKeys.ENTITY_TYPE, path)
    private fun ofStructure(path: String) = of(RegistryKeys.STRUCTURE, path)
    private fun ofBlock(path: String) = of(RegistryKeys.BLOCK, path)

    val NOT_PLACEABLE = ofItem("not_placeable")

    val MINIBOSS = ofEntity("bosses/miniboss")
    val BASIC_BOSS = ofEntity("bosses/basic")
    val EARLY_BOSS = ofEntity("bosses/early")
    val MID_BOSS = ofEntity("bosses/mid")
    val FINAL_BOSS = ofEntity("bosses/final")
    val DLC_BOSS = ofEntity("bosses/dlc")

    val INCREASES_LEVEL_CAP = ofEntity("increases_level_cap")
    val IGNORES_SKILL_SOURCE = ofEntity("ignores_skill_source")
    val BOSS_ATTRIBUTE_AFFECTED = ofEntity("boss_attribute_affected")

    val MID_STRUCTURES = ofStructure("skill/mid")
    val HARD_STRUCTURES = ofStructure("skill/hard")
    val BOSS_STRUCTURES = ofStructure("skill/boss")

    val NON_SKILL_POINT_DROP = ofBlock("non_skill_point_drop")

    val SKILL_ORDER = of(Skill, "order")
    val CLASS_ORDER = of(SkillClass, "order")
}
