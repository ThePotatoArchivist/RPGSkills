package archives.tater.rpgskills.data

import archives.tater.rpgskills.mixin.data.IngredientAccessor
import archives.tater.rpgskills.mixin.data.StackEntryAccessor
import archives.tater.rpgskills.mixin.data.TagEntryAccessor
import com.mojang.serialization.Codec
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

@Suppress("CAST_NEVER_SUCCEEDS")
val INGREDIENT_CODEC: Codec<Ingredient> = AlternateCodec(
    Registries.ITEM.codec.xmap(
        { StackEntryAccessor.newStackEntry(it.defaultStack) },
        { (it as StackEntryAccessor).stack.item }
    ),
    TagKey.codec(RegistryKeys.ITEM).xmap(
        { TagEntryAccessor.newTagEntry(it) },
        { (it as TagEntryAccessor).tag }
    )
) { it is Ingredient.TagEntry }.listOf().xmap(
    { IngredientAccessor.invokeOfEntries(it.stream()) },
    { (it as IngredientAccessor).entries.toList() }
)
