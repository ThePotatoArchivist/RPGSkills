package archives.tater.rpgskills.mixin.client.locking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.text.Style;

@Mixin(EnchantingPhrases.class)
public interface EnchantingPhrasesAccessor {
    @Accessor
    static Style getSTYLE() {
        throw new AssertionError();
    }
}
