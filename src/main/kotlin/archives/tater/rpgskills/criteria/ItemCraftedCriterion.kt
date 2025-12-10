package archives.tater.rpgskills.criteria

import archives.tater.rpgskills.criteria.ItemCraftedCriterion.Conditions
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.item.ItemStack
import net.minecraft.predicate.NumberRange
import net.minecraft.predicate.NumberRange.IntRange
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.server.network.ServerPlayerEntity
import java.util.Optional
import java.util.function.Predicate

object ItemCraftedCriterion : AbstractCriterion<Conditions>() {
    override fun getConditionsCodec(): Codec<Conditions> = Conditions.CODEC

    fun trigger(player: ServerPlayerEntity, item: ItemStack, amount: Int) {
        val tester = Predicate<Conditions> { it.matches(item) }
        repeat(amount) {
            trigger(player, tester)
        }
    }

    @JvmRecord
    data class Conditions(private val player: Optional<LootContextPredicate>, val item: ItemPredicate) : AbstractCriterion.Conditions {
        constructor(item: ItemPredicate, player: LootContextPredicate? = null) :
                this(Optional.ofNullable(player), item)

        override fun player() = player

        fun matches(item: ItemStack) = this.item.test(item)

        companion object {
            val CODEC: Codec<Conditions> = RecordCodecBuilder.create { it.group(
                LootContextPredicate.CODEC.optionalFieldOf("player").forGetter { conditions -> conditions.player },
                ItemPredicate.CODEC.fieldOf("item").forGetter(Conditions::item),
            ).apply(it, ::Conditions) }
        }
    }
}