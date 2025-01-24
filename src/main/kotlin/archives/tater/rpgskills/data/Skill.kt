package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.field
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

class Skill(
    val icon: ItemStack,
    val levels: List<Level>
) {
    companion object {
        val CODEC: Codec<Skill> = RecordCodecBuilder.create {
            it.group(
                ItemStack.CODEC.fieldOf("icon").forGetter(Skill::icon),
                Level.CODEC.listOf().fieldOf("levels").forGetter(Skill::levels)
            ).apply(it, ::Skill)
        }

        val REGISTRY_KEY = RegistryKey.ofRegistry<Skill>(RPGSkills.id("skills"))
    }

    class Level(
        val cost: Int,
        val attributes: Map<EntityAttribute, Float> = mapOf(),
        val unlockItems: List<Item> = listOf(),
        val unlockTags: List<TagKey<Item>> = listOf(),
        val unlockRecipes: List<Identifier> = listOf(),
    ) {
        companion object {
            val CODEC: Codec<Level> = RecordCodecBuilder.create {
                it.group(
                    field("cost", Level::cost, Codec.INT),
                    field("attributes", Level::attributes, mapOf(), Codec.unboundedMap(Registries.ATTRIBUTE.codec, Codec.FLOAT)),
                    field("unlock_items", Level::unlockItems, listOf(), Registries.ITEM.codec.listOf()),
                    field("unlock_tags", Level::unlockTags, listOf(), TagKey.codec(RegistryKeys.ITEM).listOf()),
                    field("unlock_recipes", Level::unlockRecipes, listOf(), Identifier.CODEC.listOf()),
                ).apply(it, ::Level)
            }
        }
    }
}

