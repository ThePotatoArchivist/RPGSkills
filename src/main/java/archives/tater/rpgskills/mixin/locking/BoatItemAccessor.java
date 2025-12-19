package archives.tater.rpgskills.mixin.locking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.BoatItem;

@Mixin(BoatItem.class)
public interface BoatItemAccessor {
    @Accessor
    boolean getChest();
}
