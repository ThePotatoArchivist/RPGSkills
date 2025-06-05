package archives.tater.rpgskills.client.render

import archives.tater.rpgskills.ItemLockTooltip
import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsClient
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
    private const val SMALL_Y = LARGE_HEIGHT

    @JvmStatic
    fun render(context: DrawContext, screen: HandledScreen<*>, x: Int, y: Int, mouseX: Int, mouseY: Int, small: Boolean = false) {
        if (RPGSkillsClient.blockedRecipeGroup == null) return

        if (small)
            context.drawGuiTexture(TEXTURE_SMALL, x, y, SMALL_WIDTH, SMALL_HEIGHT)
        else
            context.drawGuiTexture(TEXTURE_LARGE, x, y, LARGE_WIDTH, LARGE_HEIGHT)

        if (screen.screenHandler.cursorStack.isEmpty && (screen as HandledScreenAccessor).focusedSlot?.hasStack() != true
            && mouseX > x && mouseX < x + (if (small) LARGE_WIDTH else SMALL_WIDTH)
            && mouseY > y && mouseY < y + (if (small) LARGE_HEIGHT else SMALL_HEIGHT)
        )
            context.drawTooltip(
                (screen as ScreenAccessor).textRenderer,
                mutableListOf<Text>(
                    RPGSkillsClient.blockedRecipeGroup!!.recipeMessage()
                ).also {
                    ItemLockTooltip.appendRequirements(RPGSkillsClient.blockedRecipeGroup!!, it)
                },
                mouseX,
                mouseY
            )
    }
}

