package archives.tater.rpgskills.condition

import archives.tater.rpgskills.RPGSkills
import com.mojang.serialization.MapCodec
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.condition.LootConditionType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object RPGSkillsConditions {
    private fun <T: LootCondition> register(path: String, codec: MapCodec<T>): LootConditionType =
        Registry.register(Registries.LOOT_CONDITION_TYPE, RPGSkills.id(path), LootConditionType(codec))

    val IS_MONSTER = register("is_monster", IsMonsterLootCondition.CODEC)
    val MAX_AGE = register("block_max_age", MaxAgeLootCondition.CODEC)

    fun register() {

    }
}
