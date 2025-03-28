package archives.tater.rpgskills.client.render

import archives.tater.rpgskills.RPGSkills.id
import archives.tater.rpgskills.RPGSkillsClient
import archives.tater.rpgskills.data.LockGroup.Manager.recipeMessage
import archives.tater.rpgskills.mixin.client.HandledScreenAccessor
import archives.tater.rpgskills.mixin.client.ScreenAccessor
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen

object CrossedArrow {
    private val TEXTURE = id("textures/gui/container/crossed_arrow.png")
    private const val LARGE_WIDTH = 28
    private const val LARGE_HEIGHT = 21
    private const val SMALL_WIDTH = 20
    private const val SMALL_HEIGHT = 18
    private const val SMALL_Y = LARGE_HEIGHT

    @JvmStatic
    fun render(context: DrawContext, screen: HandledScreen<*>, x: Int, y: Int, mouseX: Int, mouseY: Int, large: Boolean = true) {
        if (RPGSkillsClient.blockedRecipeGroup == null) return

        val width = if (large) LARGE_WIDTH else SMALL_WIDTH
        val height = if (large) LARGE_HEIGHT else SMALL_HEIGHT

        context.drawTexture(
            TEXTURE,
            x,
            y,
            0f,
            if (large) 0f else SMALL_Y.toFloat(),
            width,
            height,
            32,
            64
        )

        if (screen.screenHandler.cursorStack.isEmpty && (screen as HandledScreenAccessor).focusedSlot?.hasStack() != true
            && mouseX > x && mouseX < x + width
            && mouseY > y && mouseY < y + height
        )
            context.drawTooltip(
                (screen as ScreenAccessor).textRenderer,
                RPGSkillsClient.blockedRecipeGroup!!.recipeMessage,
                mouseX,
                mouseY
            )
    }
}

