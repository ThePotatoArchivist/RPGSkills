package archives.tater.rpgskills.client.gui.screen

import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

abstract class AbstractSkillsScreen(private val player: PlayerEntity, title: Text) : Screen(title) {

}
