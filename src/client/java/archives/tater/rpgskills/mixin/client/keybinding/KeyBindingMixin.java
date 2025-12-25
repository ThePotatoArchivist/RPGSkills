package archives.tater.rpgskills.mixin.client.keybinding;

import archives.tater.rpgskills.client.ScreenKeyBinding;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Key;

import java.util.Map;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    @Shadow
    @Final
    private static Map<Key, KeyBinding> KEY_TO_BINDINGS;

    @WrapWithCondition(
            method = {
                    "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V",
                    "updateKeysByCode"
            },
            at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private static <K, V> boolean preventPutScreenKey(Map<K, V> instance, K k, V v) {
        return instance != KEY_TO_BINDINGS || !(v instanceof ScreenKeyBinding);
    }
}
