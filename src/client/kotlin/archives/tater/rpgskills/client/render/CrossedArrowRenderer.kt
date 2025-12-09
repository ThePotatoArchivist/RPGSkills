package archives.tater.rpgskills.client.render

import archives.tater.rpgskills.ItemLockTooltip
import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsClient
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.mixin.client.locking.HandledScreenAccessor
import archives.tater.rpgskills.mixin.client.locking.ScreenAccessor
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.text.Text

object CrossedArrowRenderer {
    private val TEXTURE_LARGE = RPGSkills.id("container/crossed_arrow")
    private val TEXTURE_SMALL = RPGSkills.id("container/crossed_arrow_small")
    private const val LARGE_WIDTH = 28
    private const val LARGE_HEIGHT = 21
    private const val SMALL_WIDTH = 20
    private const val SMALL_HEIGHT = 18

    @JvmStatic
    fun render(context: DrawContext, screen: HandledScreen<*>, x: Int, y: Int, mouseX: Int, mouseY: Int, message: (LockGroup) -> Text, small: Boolean = false) {
        if (RPGSkillsClient.uiActionLockGroup == null) return

        if (small)
            context.drawGuiTexture(TEXTURE_SMALL, x, y, SMALL_WIDTH, SMALL_HEIGHT)
        else
            context.drawGuiTexture(TEXTURE_LARGE, x, y, LARGE_WIDTH, LARGE_HEIGHT)

        renderTooltip(context, screen, x, y, if (small) LARGE_WIDTH else SMALL_WIDTH, if (small) LARGE_HEIGHT else SMALL_HEIGHT, mouseX, mouseY, message)
    }

    @JvmStatic
    fun renderTooltip(
        context: DrawContext,
        screen: HandledScreen<*>,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        mouseX: Int,
        mouseY: Int,
        message: (LockGroup) -> Text,
    ) {
        val blockedGroup = RPGSkillsClient.uiActionLockGroup ?: return

        if (!screen.screenHandler.cursorStack.isEmpty
            || (screen as HandledScreenAccessor).focusedSlot?.hasStack() == true
            || mouseX <= x || mouseX >= x + width || mouseY <= y || mouseY >= y + height
            ) return

        context.drawTooltip(
            (screen as ScreenAccessor).textRenderer,
            mutableListOf(
                message(blockedGroup)
            ).also {
                ItemLockTooltip.appendRequirements(blockedGroup, it)
            },
            mouseX,
            mouseY
        )
    }
}

