package archives.tater.rpgskills.mixin;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Ingredient.Entry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.stream.Stream;

@Mixin(Ingredient.class)
public interface IngredientAccessor {
    @Invoker
    static Ingredient invokeOfEntries(Stream<? extends Entry> entries) {
        throw new AssertionError();
    }

    @Accessor
    Entry[] getEntries();
}
