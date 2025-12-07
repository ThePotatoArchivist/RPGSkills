package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.cca.JobsComponent
import archives.tater.rpgskills.client.gui.widget.ActiveJobWidget
import archives.tater.rpgskills.client.gui.widget.AutoScrollingWidget
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.client.gui.widget.AvailableJobWidget
import archives.tater.rpgskills.client.util.drawCenteredText
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
import net.minecraft.text.Text

class JobsScreen(private val player: PlayerEntity) : AbstractSkillsScreen(player, Text.empty()) {
    private var x = 0
    private var y = 0

    private var activeCount = 0
    private var availableCount = 0

    private val totalJobs = player.registryManager[Job].streamKeys().count()

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
            jobs.active
                .map { instance -> ActiveJobWidget(instance.job, jobs, 166, x + 10, 0) }
                .toList()
        ))

        addDrawableChild(AutoScrollingWidget(x + 194, y + 19, 138, 148,
            player.registryManager[Job].streamEntriesOrdered(RPGSkillsTags.JOB_ORDER)
                .filter { it in jobs.available && it !in jobs }
                .map { job -> AvailableJobWidget(job, jobs, x + 195, 0).also { addSelectableChild(it) } }
                .toList()
        ))

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE) { close() }.apply {
            width(200)
            position(width / 2 - 100, height - 25)
        }.build())

        availableCount = jobs.available.size
        activeCount = jobs.active.size
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta)
        context.drawTexture(TEXTURE, x, y, 0f, 0f, WIDTH, HEIGHT, 512, 256)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val jobs = player[JobsComponent]

        if (activeCount != jobs.active.size || availableCount != jobs.available.size)
            clearAndInit()

        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredText(textRenderer, ACTIVE.text(jobs.active.size, JobsComponent.MAX_JOBS), x + 97, y + 7, 0x404040)
        context.drawCenteredText(textRenderer, AVAILABLE.text(jobs.available.size, totalJobs), x + 262, y + 7, 0x404040)
    }

    override fun shouldPause(): Boolean = false // TODO remove, for testing only

    companion object {
        val ACTIVE = Translation.arg("screen.$MOD_ID.jobs.active")
        val AVAILABLE = Translation.arg("screen.$MOD_ID.jobs.available")

        val TEXTURE = RPGSkills.id("textures/gui/jobs.png")

        const val WIDTH = 341
        const val HEIGHT = 176
    }
}
