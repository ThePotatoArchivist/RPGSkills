package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.client.gui.widget.AutoScrollingWidget
import archives.tater.rpgskills.client.gui.widget.JobWidget
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity

class JobsScreen(private val player: PlayerEntity) : AbstractSkillsScreen(player, TITLE.text) {
    private var x = 0
    private var y = 0

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        addDrawableChild(AutoScrollingWidget(x + 9, y + 19, 178, 148, player[SkillsComponent].let {
            it.jobs.map { (job, _) -> JobWidget(it, job, 168, x + 10, 0) }
        }))
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta)
        context.drawTexture(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawText(textRenderer, title, x + 8, y + 7, 0x404040, false)
        drawXpBar(context, x + 188, y + 6)
    }

    companion object {
        val TITLE = Translation.unit("screen.$MOD_ID.jobs")

        val TEXTURE = RPGSkills.id("textures/gui/jobs.png")

        const val WIDTH = 196
        const val HEIGHT = 176
    }
}