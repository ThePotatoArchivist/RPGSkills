package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.networking.RecipeBlockedPacket
import archives.tater.rpgskills.util.get
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.registry.entry.RegistryEntry

object RPGSkillsClient : ClientModInitializer {
	@JvmField
	var blockedRecipeGroup: RegistryEntry<LockGroup>? = null

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientPlayNetworking.registerGlobalReceiver(RecipeBlockedPacket.TYPE) { packet, player, _ ->
			blockedRecipeGroup = packet.lockGroup?.let {
				player.world.registryManager[LockGroup].getEntry(it).orElse(null)
			}
		}
		ScreenEvents.BEFORE_INIT.register { _, _, _, _ ->
			blockedRecipeGroup = null
		}
	}
}