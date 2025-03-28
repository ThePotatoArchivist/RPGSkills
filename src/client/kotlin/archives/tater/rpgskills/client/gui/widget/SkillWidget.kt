package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.client.gui.screen.SkillsScreen
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.networking.SkillUpgradePacket
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry

class SkillWidget(x: Int, y: Int, width: Int, height: Int, val player: PlayerEntity, val skill: RegistryEntry<Skill>, var cost: Int) :
    ClickableWidget(x, y, width, height, skill.name) {

    private val textRenderer = MinecraftClient.getInstance().textRenderer

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT)
        context.drawItem(skill.value.icon, x + 3, y + 3)
        context.drawText(textRenderer, skill.name, x + 21, y + 4, 0xffffff, false)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (player[SkillsComponent].canUpgrade(skill))
            ClientPlayNetworking.send(SkillUpgradePacket(skill))
    }

    companion object {
        val TEXTURE = SkillsScreen.TEXTURE
        const val WIDTH = 227
        const val HEIGHT = 22
    }
}
