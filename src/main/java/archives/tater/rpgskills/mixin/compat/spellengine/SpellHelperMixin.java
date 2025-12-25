package archives.tater.rpgskills.mixin.compat.spellengine;

import archives.tater.rpgskills.data.LockGroup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast.Attempt;

// Yes I shouldn't be mixining to an internal class but there doesn't seem to be a good alternative right now
@Mixin(SpellHelper.class)
public class SpellHelperMixin {
    @Inject(
            method = "attemptCasting(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Identifier;Z)Lnet/spell_engine/internals/casting/SpellCast$Attempt;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void checkLock(PlayerEntity player, ItemStack itemStack, Identifier spellId, boolean checkAmmo, CallbackInfoReturnable<Attempt> cir) {
        var lockGroup = LockGroup.findLocked(player, itemStack);
        if (lockGroup == null) return;
        player.sendMessage(lockGroup.itemMessage(), true);
        cir.setReturnValue(Attempt.none());
    }
}
