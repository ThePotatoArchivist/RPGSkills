package archives.tater.rpgskills.mixin.job;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancement.AdvancementCriterion;

@Mixin(AdvancementCriterion.class)
public interface AdvancementCriterionAccessor {
    @Accessor
    static MapCodec<AdvancementCriterion<?>> getMAP_CODEC() {
        throw new AssertionError();
    }
}
