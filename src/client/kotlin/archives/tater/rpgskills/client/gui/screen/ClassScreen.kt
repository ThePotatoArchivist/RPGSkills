package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.client.gui.widget.ClassNavButtonWidget
import archives.tater.rpgskills.client.gui.widget.SkillDisplayWidget
import archives.tater.rpgskills.client.gui.widget.WrappedTextWidget
import archives.tater.rpgskills.client.util.confirmScreen
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.networking.ClassChoicePayload
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.streamEntriesOrdered
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

// NOTE: will crash if no classes exist
class ClassScreen(
    val player: PlayerEntity,
) : Screen(Text.empty()), Paged {
    private var x = 0
    private var y = 0

    private val classes = player.registryManager[SkillClass].streamEntriesOrdered(RPGSkillsTags.CLASS_ORDER).toList()
    private val skills = player.registryManager[Skill].streamEntriesOrdered(RPGSkillsTags.SKILL_ORDER).toList()

    override var selectedPage = 0
        set(value) {
            field = value.mod(classes.size)
            clearAndInit()
        }
    private val selectedClass get() = classes[selectedPage]

    override fun init() {
        x = (width - WIDTH) / 2
        y = (height - HEIGHT) / 2

        val selectedClass = selectedClass

        addDrawableChild(WrappedTextWidget(x + 9, y + 26, STATS_LEFT - 1 - MARGIN, 4, Text.literal(selectedClass.value.description), 0x404040, textRenderer))
//            AutoScrollingWidget(
//                x + 9, y + 26, 116, 129, .withFirst(
//                )
//            )

        var currentY = y + MARGIN + 2
        for (skill in skills) {
            val level = selectedClass.value.startingLevels[skill] ?: 0
            addDrawableChild(SkillDisplayWidget(x + STATS_LEFT, currentY, skill, level).also {
                currentY += it.height
            })
        }

        addDrawableChild(ClassNavButtonWidget(this, x - ClassNavButtonWidget.WIDTH - BUTTON_GAP, y + HEIGHT / 2 - ClassNavButtonWidget.HEIGHT / 2, false))
        addDrawableChild(ClassNavButtonWidget(this, x + WIDTH + BUTTON_GAP, y + HEIGHT / 2 - ClassNavButtonWidget.HEIGHT / 2, true))
        addDrawableChild(ButtonWidget.builder(SELECT.text) {
            client?.setScreen(confirmScreen(
                CHOICE.text(selectedClass.value.name),
                WARNING.text
            ) {
                if (it) {
                    ClientPlayNetworking.send(ClassChoicePayload(selectedClass))
                    close()
                } else
                    client?.setScreen(this)
            })
        }.position(x, y + HEIGHT + BUTTON_GAP).width(WIDTH).build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        val selectedClass = selectedClass.value
        context.drawItem(selectedClass.icon, x + 8, y + 6)
        context.drawText(textRenderer, Text.of(selectedClass.name), x + 30, y + 10, 0x404040, false)
    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta)
        context.drawTexture(TEXTURE, x, y, 0f, 0f, WIDTH, HEIGHT, 512, 256)
    }

    override fun shouldCloseOnEsc(): Boolean = false

    override fun shouldPause(): Boolean = false // TODO remove, for testing only

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/class.png")

        const val WIDTH = 301
        const val HEIGHT = 187
        const val MARGIN = 8

        const val STATS_LEFT = 165
        const val DESCRIPTION_WIDTH = 117

        const val BUTTON_GAP = 8

        val SELECT = Translation.unit("screen.rpgskills.class.select")
        val CHOICE = Translation.arg("screen.rpgskills.class.class_choice")
        val WARNING = Translation.unit("screen.rpgskills.class.warning")
    }
}