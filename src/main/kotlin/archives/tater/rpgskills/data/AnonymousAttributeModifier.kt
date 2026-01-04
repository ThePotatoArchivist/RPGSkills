package archives.tater.rpgskills.data

import archives.tater.rpgskills.util.AlternateCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import net.minecraft.util.Identifier

@JvmRecord
data class AnonymousAttributeModifier(
    val amount: Double,
    val operation: Operation = Operation.ADD_VALUE,
) {
    fun build(identifier: Identifier, multiplier: Double = 1.0) = EntityAttributeModifier(identifier, amount * multiplier, operation)

    companion object {
        val CODEC: Codec<AnonymousAttributeModifier> = RecordCodecBuilder.create { it.group(
            Codec.DOUBLE.fieldOf("amount").forGetter(AnonymousAttributeModifier::amount),
            Operation.CODEC.optionalFieldOf("operation", Operation.ADD_VALUE).forGetter(AnonymousAttributeModifier::operation)
        ).apply(it, ::AnonymousAttributeModifier) }

        fun shortCodec(defaultOperation: Operation = Operation.ADD_VALUE): AlternateCodec<AnonymousAttributeModifier> = AlternateCodec(
            CODEC,
            Codec.DOUBLE.xmap({ AnonymousAttributeModifier(it, defaultOperation) }, { it.amount })
        ) { it.operation == defaultOperation }
    }
}