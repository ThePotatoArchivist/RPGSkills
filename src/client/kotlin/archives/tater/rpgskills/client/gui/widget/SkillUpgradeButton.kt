package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.client.gui.screen.SkillsScreen
import archives.tater.rpgskills.client.util.drawOutlinedText
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.networking.SkillUpgradePayload
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.get
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text

class SkillUpgradeButton(
    x: Int,
    y: Int,
    player: PlayerEntity,
    private val skill: RegistryEntry<Skill>
) : ClickableWidget(x, y, WIDTH, HEIGHT, Text.empty()) {
    private val skillsComponent = player[SkillsComponent]

    private val cost get() = skillsComponent.getUpgradeCost(skill)
    private val canUpgrade get() = skillsComponent.canUpgrade(skill)

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (canUpgrade)
            ClientPlayNetworking.send(SkillUpgradePayload(skill))
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val canUpgrade = canUpgrade // Performance
        val cost = cost

        context.drawTexture(TEXTURE, x, y, 61 + when {
            cost == null -> 3 * WIDTH
            !canUpgrade -> 2 * WIDTH
            isHovered -> 1 * WIDTH
            else -> 0
        }, 192, WIDTH, HEIGHT)

        if (cost == null) {
            context.drawOutlinedText(textRenderer, MAX.text, x + (WIDTH - textRenderer.getWidth(MAX.text) - 2) / 2, y + (HEIGHT - 9) / 2, 0x70DACD)
            return
        }

        context.drawOutlinedText(
            textRenderer,
            cost.toString(),
            x + 12,
            y + 5,
            if (canUpgrade) 0x70DACD else 0x8C605D, // TODO better color
            if (canUpgrade) 0 else 0x47352F
        )
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    companion object {
        val TEXTURE = SkillsScreen.TEXTURE
        const val WIDTH = 36
        const val HEIGHT = 18

        val MAX = Translation.unit("screen.rpgskills.skills.max")

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }
}
