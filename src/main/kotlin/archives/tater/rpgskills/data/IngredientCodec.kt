package archives.tater.rpgskills.data

import archives.tater.rpgskills.mixin.data.IngredientAccessor
import archives.tater.rpgskills.mixin.data.StackEntryAccessor
import archives.tater.rpgskills.mixin.data.TagEntryAccessor
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

val INGREDIENT_CODEC = Codec.either(
    TagKey.codec(RegistryKeys.ITEM).xmap(
        { TagEntryAccessor.newTagEntry(it) },
        { (it as TagEntryAccessor).tag }
    ),
    Registries.ITEM.codec.xmap(
        { StackEntryAccessor.newStackEntry(it.defaultStack) },
        { (it as StackEntryAccessor).stack.item }
    )
).xmap(
    { either -> either.map(
        { it as Ingredient.Entry },
        { it as Ingredient.Entry }
    ) },
    {
        when (it) {
            is Ingredient.TagEntry -> Either.left(it)
            is Ingredient.StackEntry -> Either.right(it)
            else -> throw AssertionError()
        }
    }
).listOf().xmap(
    { IngredientAccessor.invokeOfEntries(it.stream()) },
    { (it as IngredientAccessor).entries.toList() }
)
