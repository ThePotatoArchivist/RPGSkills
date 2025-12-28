package archives.tater.rpgskills.condition

import com.mojang.serialization.MapCodec
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.condition.LootConditionType
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.state.property.Properties

object MaxAgeLootCondition : LootCondition {
    override fun getType(): LootConditionType = RPGSkillsConditions.MAX_AGE

    override fun test(context: LootContext): Boolean =
        context[LootContextParameters.BLOCK_STATE]?.let {
            ages.any { (property, max) -> property in it && it[property] == max }
        } == true

    val CODEC: MapCodec<MaxAgeLootCondition> = MapCodec.unit(this)

    private val ages = listOf(
        Properties.AGE_1,
        Properties.AGE_2,
        Properties.AGE_3,
        Properties.AGE_4,
        Properties.AGE_5,
        Properties.AGE_7,
        Properties.AGE_15,
        Properties.AGE_25,
    ).associateWith { it.values.max() }
}