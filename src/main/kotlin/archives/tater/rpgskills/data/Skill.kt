package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.mixin.data.IngredientAccessor
import archives.tater.rpgskills.mixin.data.StackEntryAccessor
import archives.tater.rpgskills.mixin.data.TagEntryAccessor
import archives.tater.rpgskills.util.RegistryKeyHolder
import archives.tater.rpgskills.util.field
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import java.util.stream.Stream

class Skill(
    val icon: ItemStack,
    val levels: List<Level>
) {
    fun unlocksItem(level: Int, stack: ItemStack) = levels.subList(0, level).any { it.unlocks.items.test(stack) }

    companion object : RegistryKeyHolder<Registry<Skill>> {
        val CODEC: Codec<Skill> = RecordCodecBuilder.create {
            it.group(
                ItemStack.CODEC.fieldOf("icon").forGetter(Skill::icon),
                Level.CODEC.listOf().fieldOf("levels").forGetter(Skill::levels)
            ).apply(it, ::Skill)
        }

        override val key: RegistryKey<Registry<Skill>> = RegistryKey.ofRegistry(RPGSkills.id("skills"))
    }

    class Level(
        val cost: Int,
        val attributes: Map<EntityAttribute, Float> = mapOf(),
        val unlocks: Locked,
    ) {
        constructor(
            cost: Int,
            attributes: Map<EntityAttribute, Float> = mapOf(),
            unlockItems: List<Item> = listOf(),
            unlockTags: List<TagKey<Item>> = listOf(),
            unlockRecipes: List<Identifier> = listOf(),
        ) : this(cost, attributes, Locked(
            IngredientAccessor.invokeOfEntries(Stream.concat(
            unlockItems.stream().map { StackEntryAccessor.newStackEntry(it.defaultStack) },
            unlockTags.stream().map { TagEntryAccessor.newTagEntry(it) }
        )), unlockRecipes))

        companion object {
            val CODEC: Codec<Level> = RecordCodecBuilder.create {
                it.group(
                    field("cost", Level::cost, Codec.INT),
                    field("attributes", Level::attributes, mapOf(), Codec.unboundedMap(Registries.ATTRIBUTE.codec, Codec.FLOAT)),
                    field("unlocks", Level::unlocks, Locked(), Locked.CODEC)
                ).apply(it, ::Level)
            }
        }
    }

    class Locked(
        val items: Ingredient = Ingredient.EMPTY,
        val recipes: List<Identifier> = listOf(),
    ) {
        companion object {
            val CODEC: Codec<Locked> = RecordCodecBuilder.create {
                it.group(
                    field("items", Locked::items, Ingredient.EMPTY, INGREDIENT_CODEC),
                    field("recipes", Locked::recipes, listOf(), Identifier.CODEC.listOf()),
                ).apply(it, ::Locked)
            }
        }
    }
}

