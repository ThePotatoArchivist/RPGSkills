package archives.tater.rpgskills.condition

import com.mojang.serialization.MapCodec
import net.minecraft.component.DataComponentTypes
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.condition.LootConditionType
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters

object SuitableToolLootCondition : LootCondition {
    override fun getType(): LootConditionType = RPGSkillsConditions.SUITABLE_TOOL

    override fun test(context: LootContext): Boolean {
        val state = context[LootContextParameters.BLOCK_STATE] ?: return false
        return context.requireParameter(LootContextParameters.TOOL)[DataComponentTypes.TOOL]
            ?.isCorrectForDrops(state) == true
    }

    val CODEC: MapCodec<SuitableToolLootCondition> = MapCodec.unit(this)
}