package archives.tater.rpgskills.client.render

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.get
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayerEntity

object SkillBarRenderer {
    private var showTicks = 0

    private const val MAX_SHOW_TICKS = 40 // TODO
    private val BACKGROUND_TEXTURE = RPGSkills.id("textures/gui/sprites/hud/skill_bar_background.png")
    private val PROGRESS_TEXTURE = RPGSkills.id("textures/gui/sprites/hud/skill_bar_progress.png")
    private const val TEXTURE_WIDTH = 182
    private const val TEXTURE_HEIGHT = 5

    @JvmStatic
    val shouldRender
        @JvmName("shouldRender")
        get() = showTicks > 0

    @JvmStatic
    fun renderBar(context: DrawContext, x: Int, clientPlayer: ClientPlayerEntity) {
        val skills = clientPlayer[SkillsComponent]
        val barWidth = (skills.levelProgress * (TEXTURE_WIDTH + 1)).toInt()
        val y = context.scaledWindowHeight - 32 + 3
        RenderSystem.enableBlend()
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0f, 0f, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT)
        if (barWidth > 0)
            context.drawTexture(PROGRESS_TEXTURE, x, y, barWidth, TEXTURE_HEIGHT, 0f, 0f, barWidth, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT)

        RenderSystem.disableBlend()
    }

    @JvmStatic
    fun renderLevel(context: DrawContext, textRenderer: TextRenderer, clientPlayer: ClientPlayerEntity) {
        val skills = clientPlayer[SkillsComponent]
        if (skills.level <= 0) return
        val display = "${skills.spendableLevels}/${skills.level}"
        val x: Int = (context.scaledWindowWidth - textRenderer.getWidth(display)) / 2
        val y = context.scaledWindowHeight - 31 - 4
        context.drawText(textRenderer, display, x + 1, y, 0, false)
        context.drawText(textRenderer, display, x - 1, y, 0, false)
        context.drawText(textRenderer, display, x, y + 1, 0, false)
        context.drawText(textRenderer, display, x, y - 1, 0, false)
        context.drawText(textRenderer, display, x, y, 0x70DACD, false)
    }

    @JvmStatic
    fun onSkillOrbPickup() {
        showTicks = MAX_SHOW_TICKS
    }

    fun register() {
        ClientTickEvents.END_WORLD_TICK.register { _ ->
            if (showTicks > 0)
                showTicks--
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            showTicks = 0
        }
    }
}
