package archives.tater.rpgskills

import archives.tater.rpgskills.client.gui.screen.SkillsScreen
import archives.tater.rpgskills.client.util.wasPressed
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.networking.RecipeBlockedPacket
import archives.tater.rpgskills.util.registryOf
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.registry.entry.RegistryEntry
import org.lwjgl.glfw.GLFW
import kotlin.jvm.optionals.getOrNull

object RPGSkillsClient : ClientModInitializer {
	@JvmField
	var blockedRecipeGroup: RegistryEntry<LockGroup>? = null

	val skillsKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
		"key.rpgskills.screen.skills",
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_O,
		"category.rpgskills.rpgskills"
	))

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientPlayNetworking.registerGlobalReceiver(RecipeBlockedPacket.TYPE) { packet, player, _ ->
			blockedRecipeGroup = packet.lockGroup?.let {
				registryOf(player, LockGroup).getEntry(it).getOrNull()
			}
		}
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			while (skillsKeyBinding.wasPressed) {
				client.player?.let {
					client.setScreen(SkillsScreen(it))
				}
			}
		}
		ScreenEvents.BEFORE_INIT.register { _, _, _, _ ->
			blockedRecipeGroup = null
		}
	}
}