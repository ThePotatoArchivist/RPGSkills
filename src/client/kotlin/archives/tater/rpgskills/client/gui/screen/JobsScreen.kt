package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.client.gui.widget.AutoScrollingWidget
import archives.tater.rpgskills.client.gui.widget.JobWidget
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.cca.JobsComponent
import archives.tater.rpgskills.networking.CloseJobScreenPayload
import archives.tater.rpgskills.networking.OpenJobScreenPayload
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.streamEntriesOrdered
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.ScreenTexts

class JobsScreen(private val player: PlayerEntity) : AbstractSkillsScreen(player, TITLE.text) {
    private var x = 0
    private var y = 0

    override fun onDisplayed() {
        ClientPlayNetworking.send(OpenJobScreenPayload)
    }

    override fun removed() {
        ClientPlayNetworking.send(CloseJobScreenPayload)
    }

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        val jobs = player[JobsComponent]
        addDrawableChild(AutoScrollingWidget(x + 9, y + 19, 178, 148,
            player.registryManager[Job].streamEntriesOrdered(RPGSkillsTags.JOB_ORDER)
                .filter { it in jobs.active }
                .map { job -> JobWidget(jobs, job, 168, x + 10, 0) }
                .toList()
        ))

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE) { close() }.apply {
            width(200)
            position(width / 2 - 100, height - 25)
        }.build())
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
