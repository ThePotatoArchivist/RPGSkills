package archives.tater.rpgskills.mixin.locking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingScreenHandler.class)
public interface CraftingScreenHandlerAccessor {
    @Accessor
    CraftingResultInventory getResult();

    @Accessor
    PlayerEntity getPlayer();
}
