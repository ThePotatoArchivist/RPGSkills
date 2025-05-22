package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.RegistryKeyHolder
import archives.tater.rpgskills.util.field
import archives.tater.rpgskills.util.value
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Text

class Skill(
    val icon: ItemStack,
    val levels: List<Level>,
    val name: String? = null,
    val description: String? = null,
) {
    companion object : RegistryKeyHolder<Registry<Skill>> {
        val CODEC: Codec<Skill> = RecordCodecBuilder.create {
            it.group(
                field("icon", Skill::icon, ItemStack.CODEC),
                field("levels", Skill::levels, Level.SHORT_CODEC.listOf()),
                field("name", Skill::name, null, Codec.STRING),
                field("description", Skill::description, null, Codec.STRING)
            ).apply(it, ::Skill)
        }

        override val key: RegistryKey<Registry<Skill>> = RegistryKey.ofRegistry(RPGSkills.id("skills"))

        val RegistryEntry<Skill>.name: MutableText get() = value.name?.let(Text::literal) ?: Text.translatable(key.get().value.toTranslationKey("skill", "name"))
        val RegistryEntry<Skill>.description: MutableText get() = value.description?.let(Text::literal) ?: Text.translatable(key.get().value.toTranslationKey("skill", "name"))
    }

    class Level(
        val cost: Int,
        val attributes: Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> = mapOf(),
    ) {
        companion object {
            val CODEC: Codec<Level> = RecordCodecBuilder.create {
                it.group(
                    field("cost", Level::cost, Codec.INT),
                    field("attributes", Level::attributes, mapOf(), Codec.unboundedMap(Registries.ATTRIBUTE.entryCodec, EntityAttributeModifier.CODEC)),
                ).apply(it, ::Level)
            }

            val SHORT_CODEC: Codec<Level> = AlternateCodec(
                CODEC,
                Codec.INT.xmap({ Level(it) }, { it.cost })
            ) { it.attributes.isEmpty() }
        }
    }
}

