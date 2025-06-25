package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.AlternateCodec
import archives.tater.rpgskills.util.RegistryKeyHolder
import archives.tater.rpgskills.util.forGetter
import archives.tater.rpgskills.util.value
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*
import kotlin.jvm.optionals.getOrNull

@JvmRecord
data class Skill(
    val icon: ItemStack,
    val levels: List<Level>,
    val name: String,
    val description: String? = null,
) {
    private constructor(
        icon: ItemStack,
        levels: List<Level>,
        name: String,
        description: Optional<String>,
    ) : this(icon, levels, name, description.getOrNull())

    companion object : RegistryKeyHolder<Registry<Skill>> {
        val CODEC: Codec<Skill> = RecordCodecBuilder.create {
            it.group(
                ItemStack.UNCOUNTED_CODEC.fieldOf("icon").forGetter(Skill::icon),
                Level.SHORT_CODEC.listOf().fieldOf("levels").forGetter(Skill::levels),
                Codec.STRING.fieldOf("name").forGetter(Skill::name),
                Codec.STRING.optionalFieldOf("description").forGetter(Skill::description)
            ).apply(it, ::Skill)
        }

        override val key: RegistryKey<Registry<Skill>> = RegistryKey.ofRegistry(RPGSkills.id("skill"))

        val RegistryEntry<Skill>.name: MutableText get() = Text.literal(value.name)
        val RegistryEntry<Skill>.description: MutableText get() = Text.literal(value.description ?: "")
    }

    @JvmRecord
    data class Level(
        val cost: Int,
        val attributes: Map<RegistryEntry<EntityAttribute>, AnonymousAttributeModifier> = mapOf(),
    ) {
        companion object {
            val CODEC: Codec<Level> = RecordCodecBuilder.create {
                it.group(
                    Codec.INT.fieldOf("cost").forGetter(Level::cost),
                    Codec.unboundedMap(Registries.ATTRIBUTE.entryCodec, AnonymousAttributeModifier.SHORT_CODEC).optionalFieldOf("attributes", mapOf()).forGetter(Level::attributes),
                ).apply(it, ::Level)
            }

            val SHORT_CODEC: Codec<Level> = AlternateCodec(
                CODEC,
                Codec.INT.xmap({ Level(it) }, { it.cost })
            ) { it.attributes.isEmpty() }
        }
    }

    @JvmRecord
    data class AnonymousAttributeModifier(
        val amount: Double,
        val operation: Operation = Operation.ADD_VALUE,
    ) {
        fun build(identifier: Identifier) = EntityAttributeModifier(identifier, amount, operation)

        companion object {
            val CODEC: Codec<AnonymousAttributeModifier> = RecordCodecBuilder.create { it.group(
                Codec.DOUBLE.fieldOf("amount").forGetter(AnonymousAttributeModifier::amount),
                Operation.CODEC.optionalFieldOf("operation", Operation.ADD_VALUE).forGetter(AnonymousAttributeModifier::operation)
            ).apply(it, ::AnonymousAttributeModifier) }

            val SHORT_CODEC: Codec<AnonymousAttributeModifier> = AlternateCodec(
                CODEC,
                Codec.DOUBLE.xmap({ AnonymousAttributeModifier(it) }, { it.amount })
            ) { it.operation == Operation.ADD_VALUE }
        }
    }
}

