package archives.tater.rpgskills.client.gui

import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.toast.Toast
import net.minecraft.client.toast.ToastManager
import net.minecraft.item.Items
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.OrderedText
import net.minecraft.util.Colors
import net.minecraft.util.Identifier

class JobCompletedToast(val job: Job, val skill: Skill?) : Toast {
    override fun draw(
        context: DrawContext,
        manager: ToastManager,
        startTime: Long
    ): Toast.Visibility {
        context.drawGuiTexture(TEXTURE, 0, 0, width, height)
        context.drawItemWithoutEntity(skill?.icon ?: Items.WRITABLE_BOOK.defaultStack, 8, 8)
        context.drawText(manager.client.textRenderer, TITLE.text, 30, 7, 0xAAAA, false)
        context.drawText(manager.client.textRenderer, job.name, 30, 18, 0xFFFFFF, false)

        return if (startTime * manager.notificationDisplayTimeMultiplier > 5000) Toast.Visibility.HIDE else Toast.Visibility.SHOW
    }

    companion object {
        val TEXTURE: Identifier = Identifier.ofVanilla("toast/advancement")

        val TITLE = Translation.unit("toast.$MOD_ID.job_complete")
    }
}