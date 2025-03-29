package archives.tater.rpgskills

import archives.tater.rpgskills.RPGSkills.MOD_ID
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

	const val RPG_SKILLS_CATEGORY = "category.$MOD_ID.$MOD_ID"
	const val SKILLS_KEY_TRANSLATION = "key.$MOD_ID.screen.skills"

	val skillsKeyBinding: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
		SKILLS_KEY_TRANSLATION,
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_O,
		RPG_SKILLS_CATEGORY,
	))

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientPlayNetworking.registerGlobalReceiver(RecipeBlockedPacket.TYPE) { packet, player, _ ->
			blockedRecipeGroup = packet.lockGroup?.let {
				registryOf(player, LockGroup).getEntry(it).getOrNull()
			}
		}

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (skillsKeyBinding.wasPressed)
				client.player?.let {
					client.setScreen(SkillsScreen(it))
				}
		}

		ScreenEvents.BEFORE_INIT.register { _, _, _, _ ->
			blockedRecipeGroup = null
		}
	}
}