package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.cca.JobsComponent
import archives.tater.rpgskills.client.gui.widget.ActiveJobWidget
import archives.tater.rpgskills.client.gui.widget.AutoScrollingWidget
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.client.gui.widget.AvailableJobWidget
import archives.tater.rpgskills.client.gui.widget.ClassNavButtonWidget
import archives.tater.rpgskills.client.gui.widget.LockedAvailableJobWidget
import archives.tater.rpgskills.client.gui.widget.SkillTabWidget
import archives.tater.rpgskills.client.util.drawCenteredText
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.networking.CloseJobScreenPayload
import archives.tater.rpgskills.networking.OpenJobScreenPayload
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.ceilDiv
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.streamEntriesOrdered
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text

class JobsScreen(private val player: PlayerEntity) : AbstractSkillsScreen(player, Text.empty()), Tabbed, Paged {
    private var x = 0
    private var y = 0

    private val skills = player.registryManager[Skill].streamEntries()
        .filter { skill -> skill.value.levels.sumOf { it.jobs.size } > 0 }
        .toList()

    override var selectedTab: Int = 0
        set(value) {
            field = value
            clearAndInit()
        }
    override var selectedPage: Int = 0
        set(value) {
            field = value.mod(skills.size ceilDiv MAX_TABS)
            clearAndInit()
        }

    private val selectedSkill get() = skills[selectedTab]

    private var activeCount = 0
    private var availableCount = 0

    val jobs = player[JobsComponent]

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

        val active = jobs.active
            .map { instance -> ActiveJobWidget(instance.job, player, 168, x + 10, 0) }
        addDrawableChild(AutoScrollingWidget(x + 9, y + 19, 178, 148, active))
        for (widget in active) addSelectableChild(widget)

        val availables = player.registryManager[Job].streamEntriesOrdered(RPGSkillsTags.JOB_ORDER)
                .filter { it !in jobs && it in selectedSkill.value }
                .sorted(compareBy { if (it in jobs.available) 0 else 1 })
                .map { job ->
                    if (job in jobs.available)
                        AvailableJobWidget(job, jobs, x + 195, 0)
                    else
                        LockedAvailableJobWidget(x + 195, 0)
                }
                .toList()
        addDrawableChild(AutoScrollingWidget(x + 194, y + 19, 138, 148, availables))
        for (button in availables) addSelectableChild(button)

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE) { close() }.apply {
            width(200)
            position(width / 2 - 100, height - 25)
        }.build())

        for (i in 0..<MAX_TABS) {
            val index = selectedPage * MAX_TABS + i
            val skill = skills.getOrNull(index) ?: break
            addDrawableChild(SkillTabWidget(x + 203 + (SkillTabWidget.WIDTH + 2) * i, y + HEIGHT - SkillTabWidget.HEIGHT, skill, index, this))
        }

        if (skills.size > MAX_TABS) {
            addDrawableChild(ClassNavButtonWidget(this, x + 203 - ClassNavButtonWidget.WIDTH - 4, y + TEXTURE_HEIGHT + 4, false))
            addDrawableChild(ClassNavButtonWidget(this, x + 320 + 4, y + TEXTURE_HEIGHT + 4, true))
        }

        availableCount = jobs.available.size
        activeCount = jobs.active.size
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta)
        context.drawTexture(TEXTURE, x, y, 0f, 0f, WIDTH, TEXTURE_HEIGHT, 512, 256)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val jobs = player[JobsComponent]
        val availableSkillJobs = jobs.available.filter { it in selectedSkill.value }

        if (activeCount != jobs.active.size || availableCount != jobs.available.size)
            clearAndInit()

        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredText(textRenderer, ACTIVE.text(jobs.active.size, JobsComponent.MAX_JOBS), x + 97, y + 7, 0x404040)
        context.drawCenteredText(textRenderer, AVAILABLE.text(availableSkillJobs.size, selectedSkill.value.levels.sumOf { it.jobs.size }), x + 262, y + 7, 0x404040)

        if (jobs.active.isEmpty())
            context.drawCenteredText(textRenderer, NO_JOBS.text, x + 98, y + 89, 0x606060)
    }

    override fun shouldPause(): Boolean = false // TODO remove, for testing only

    companion object {
        val ACTIVE = Translation.arg("screen.$MOD_ID.jobs.active")
        val AVAILABLE = Translation.arg("screen.$MOD_ID.jobs.available")
        val NO_JOBS = Translation.unit("screen.$MOD_ID.jobs.no_jobs")

        val TEXTURE = RPGSkills.id("textures/gui/jobs.png")

        const val WIDTH = 341
        const val TEXTURE_HEIGHT = 176
        const val HEIGHT = 204

        const val MAX_TABS = 4
    }
}
