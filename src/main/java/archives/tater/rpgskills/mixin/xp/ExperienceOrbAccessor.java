package archives.tater.rpgskills.mixin.xp;

import net.minecraft.entity.ExperienceOrbEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ExperienceOrbEntity.class)
public interface ExperienceOrbAccessor {
    @Accessor
    void setAmount(int amount);
}
