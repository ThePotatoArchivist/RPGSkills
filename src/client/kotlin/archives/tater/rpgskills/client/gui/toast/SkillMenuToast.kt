package archives.tater.rpgskills.client.gui.toast

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsClient
import archives.tater.rpgskills.util.Translation
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.toast.Toast
import net.minecraft.client.toast.ToastManager
import net.minecraft.util.Identifier

class SkillMenuToast(seen: Boolean = false) : TrackedToast(seen) {
    override fun draw(
        context: DrawContext,
        manager: ToastManager,
        startTime: Long
    ): Toast.Visibility {
        context.drawGuiTexture(TEXTURE, 0, 0, width, height)
        context.drawGuiTexture(ICON, 8, 8, 16, 16)
        context.drawTextWrapped(manager.client.textRenderer, TITLE.text(RPGSkillsClient.skillsKey.boundKeyLocalizedText), 30, 7, width - 38, 0xAAAAAA)

        return if (shown) Toast.Visibility.SHOW else Toast.Visibility.HIDE
    }

    companion object {
        val TEXTURE: Identifier = Identifier.ofVanilla("toast/advancement")

        val ICON = RPGSkills.id("skill/skill_orb")

        val TITLE = Translation.arg("toast.${RPGSkills.MOD_ID}.skill_menu")

        val CODEC = createCodec(::SkillMenuToast)
    }
}