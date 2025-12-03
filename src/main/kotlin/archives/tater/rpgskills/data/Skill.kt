package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.RegistryKeyHolder
import archives.tater.rpgskills.util.SHORT_STACK_CODEC
import archives.tater.rpgskills.util.forGetter
import archives.tater.rpgskills.util.indexedOf
import archives.tater.rpgskills.util.value
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryFixedCodec
import net.minecraft.text.MutableText
import net.minecraft.text.Text
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
                SHORT_STACK_CODEC.fieldOf("icon").forGetter(Skill::icon),
                Level.CODEC.indexedOf(default = Level()).fieldOf("levels").forGetter(Skill::levels),
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
        val attributes: Map<RegistryEntry<EntityAttribute>, AnonymousAttributeModifier> = mapOf(),
        val jobs: List<RegistryEntry<Job>> = listOf(),
    ) {
        companion object {
            val CODEC: Codec<Level> = RecordCodecBuilder.create {
                it.group(
                    Codec.unboundedMap(Registries.ATTRIBUTE.entryCodec, AnonymousAttributeModifier.SHORT_CODEC).optionalFieldOf("attributes", mapOf()).forGetter(Level::attributes),
                    RegistryFixedCodec.of(Job.key).listOf().optionalFieldOf("jobs", listOf()).forGetter(Level::jobs),
                ).apply(it, ::Level)
            }
        }
    }

}

