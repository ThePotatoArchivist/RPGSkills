package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.SimpleSynchronousResourceReloadListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object RPGSkills : ModInitializer {
	const val MOD_ID = "rpgskills"

	@JvmStatic
	fun id(path: String) = Identifier(MOD_ID, path)

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		DynamicRegistries.registerSynced(Skill.key, Skill.CODEC)
		DynamicRegistries.registerSynced(LockGroup.key, LockGroup.CODEC)

		CommandRegistrationCallback.EVENT.register(RPGSkillsCommands)

		ResourceManagerHelper.registerBuiltinResourcePack(
			id("default_pack"),
			FabricLoader.getInstance().getModContainer(MOD_ID).get(),
			ResourcePackActivationType.NORMAL
		)

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(SimpleSynchronousResourceReloadListener(id("clear_locked_items")) {
			LockGroup.clearLocked()
		})
	}
}
