package archives.tater.rpgskills.client

import archives.tater.rpgskills.mixin.client.keybinding.KeyBindingAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

/**
 * @see [archives.tater.rpgskills.mixin.client.keybinding.KeyBindingMixin]
 */
class ScreenKeyBinding(translationKey: String, code: Int, category: String) : KeyBinding(translationKey, InputUtil.Type.KEYSYM, code, category) {
    @Suppress("CAST_NEVER_SUCCEEDS")
    val isDown get() = InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, (this as KeyBindingAccessor).boundKey.code)

    @Deprecated("Not functional for screen keys")
    override fun isPressed(): Boolean {
        return super.isPressed()
    }

    @Deprecated("Not functional for screen keys")
    override fun wasPressed(): Boolean {
        return super.wasPressed()
    }
}