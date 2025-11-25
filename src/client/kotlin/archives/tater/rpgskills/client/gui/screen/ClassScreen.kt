package archives.tater.rpgskills.client.gui.screen

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.gui.widget.AutoScrollingWidget
import archives.tater.rpgskills.client.gui.widget.ClassNavButtonWidget
import archives.tater.rpgskills.client.gui.widget.SkillDisplayWidget
import archives.tater.rpgskills.client.gui.widget.WrappedTextWidget
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.networking.ClassChoicePayload
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import archives.tater.rpgskills.util.withFirst
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

    private val classes = player.registryManager[SkillClass].streamEntries().toList()

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

        addDrawableChild(
            AutoScrollingWidget(
                x + 9, y + 26, 116, 129, selectedClass.value.startingLevels.map { (skill, level) ->
                    SkillDisplayWidget(x + 9, 0, 116 - AutoScrollingWidget.SCROLLER_WIDTH, skill, level)
                }.withFirst(
                    WrappedTextWidget(x + 9, 0, 116 - AutoScrollingWidget.SCROLLER_WIDTH, 4, Text.literal(selectedClass.value.description), 0x404040, textRenderer)
                )
            )
        )

        addDrawableChild(ClassNavButtonWidget(this, x - ClassNavButtonWidget.WIDTH - BUTTON_GAP, y + HEIGHT / 2 - ClassNavButtonWidget.HEIGHT / 2, false))
        addDrawableChild(ClassNavButtonWidget(this, x + WIDTH + BUTTON_GAP, y + HEIGHT / 2 - ClassNavButtonWidget.HEIGHT / 2, true))
        addDrawableChild(ButtonWidget.builder(SELECT.text) {
            ClientPlayNetworking.send(ClassChoicePayload(selectedClass))
            close()
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
        context.drawTexture(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT)
    }

    override fun shouldCloseOnEsc(): Boolean = false

    override fun shouldPause(): Boolean = false // TODO remove, for testing only

    companion object {
        val TEXTURE = RPGSkills.id("textures/gui/class.png")

        const val WIDTH = 134
        const val HEIGHT = 164

        const val BUTTON_GAP = 8

        val SELECT = Translation.unit("screen.rpgskills.class.select")
    }
}