package archives.tater.rpgskills.data

import archives.tater.rpgskills.util.AlternateCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.util.Identifier

@JvmRecord
data class AnonymousAttributeModifier(
    val amount: Double,
    val operation: EntityAttributeModifier.Operation = EntityAttributeModifier.Operation.ADD_VALUE,
) {
    fun build(identifier: Identifier, multiplier: Double = 1.0) = EntityAttributeModifier(identifier, amount * multiplier, operation)

    companion object {
        val CODEC: Codec<AnonymousAttributeModifier> = RecordCodecBuilder.create { it.group(
            Codec.DOUBLE.fieldOf("amount").forGetter(AnonymousAttributeModifier::amount),
            EntityAttributeModifier.Operation.CODEC.optionalFieldOf("operation", EntityAttributeModifier.Operation.ADD_VALUE).forGetter(AnonymousAttributeModifier::operation)
        ).apply(it, ::AnonymousAttributeModifier) }

        val SHORT_CODEC: Codec<AnonymousAttributeModifier> = AlternateCodec(
            CODEC,
            Codec.DOUBLE.xmap({ AnonymousAttributeModifier(it) }, { it.amount })
        ) { it.operation == EntityAttributeModifier.Operation.ADD_VALUE }
    }
}