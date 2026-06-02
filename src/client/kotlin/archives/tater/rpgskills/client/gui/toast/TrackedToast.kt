package archives.tater.rpgskills.client.gui.toast

import archives.tater.rpgskills.RPGSkillsClient
import com.mojang.serialization.Codec
import net.minecraft.client.MinecraftClient
import net.minecraft.client.toast.Toast

abstract class TrackedToast(private var seen: Boolean) : Toast {
    var shown = true
        private set

    fun show(client: MinecraftClient) {
        if (seen || !shown) return
        client.toastManager.add(this)
        seen = true
        RPGSkillsClient.STATE.write()
    }

    fun hide() {
        shown = false
    }

    companion object {
        fun <T: TrackedToast> createCodec(factory: (Boolean) -> T): Codec<T> = Codec.BOOL.xmap(factory, TrackedToast::seen)
    }
}