package archives.tater.rpgskills

import archives.tater.rpgskills.data.Skill
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object RPGSkills : ModInitializer {
	const val MOD_ID = "rpgskills"

	fun id(path: String) = Identifier(MOD_ID, path)

    private val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		DynamicRegistries.registerSynced(Skill.REGISTRY_KEY, Skill.CODEC)

		CommandRegistrationCallback.EVENT.register(RPGSkillsCommands)
	}
}
