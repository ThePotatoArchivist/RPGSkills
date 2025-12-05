package archives.tater.rpgskills.condition

import com.mojang.serialization.MapCodec
import net.minecraft.entity.mob.Monster
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.condition.LootConditionType
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameter


@JvmRecord
data class IsMonsterLootCondition(val entity: LootContext.EntityTarget) : LootCondition {
    override fun getType(): LootConditionType = RPGSkillsConditions.IS_MONSTER

    override fun getRequiredParameters(): Set<LootContextParameter<*>> = setOf(entity.parameter)

    override fun test(context: LootContext): Boolean = context.get(entity.parameter) is Monster

    companion object {
        val CODEC: MapCodec<IsMonsterLootCondition> = LootContext.EntityTarget.CODEC.fieldOf("entity")
            .xmap(::IsMonsterLootCondition, IsMonsterLootCondition::entity)
    }
}
