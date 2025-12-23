package archives.tater.rpgskills.mixin.client.keybinding;

import archives.tater.rpgskills.client.ScreenKeyBinding;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry;
import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBindingEntry.class)
public class KeyBindingEntryMixin {
    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;equals(Lnet/minecraft/client/option/KeyBinding;)Z")
    )
    private boolean preventDuplicateScreenKeybind(KeyBinding instance, KeyBinding other, Operation<Boolean> original) {
        return original.call(instance, other) && (instance instanceof ScreenKeyBinding == other instanceof ScreenKeyBinding);
    }
}
