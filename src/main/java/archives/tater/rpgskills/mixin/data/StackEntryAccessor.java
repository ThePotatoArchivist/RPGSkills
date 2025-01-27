package archives.tater.rpgskills.mixin.data;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ingredient.StackEntry.class)
public interface StackEntryAccessor {
    @Invoker("<init>")
    static Ingredient.StackEntry newStackEntry(ItemStack stack) {
        throw new AssertionError();
    }

    @Accessor
    ItemStack getStack();
}
