package archives.tater.rpgskills.mixin;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ingredient.TagEntry.class)
public interface TagEntryAccessor {
    @Invoker("<init>")
    static Ingredient.TagEntry newTagEntry(TagKey<Item> tag) {
        throw new AssertionError();
    }

    @Accessor
    TagKey<Item> getTag();
}
