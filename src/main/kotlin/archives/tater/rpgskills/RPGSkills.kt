package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.item.RPGSkillsItems
import archives.tater.rpgskills.networking.ChooseClassPayload
import archives.tater.rpgskills.networking.ClassChoicePayload
import archives.tater.rpgskills.networking.RecipeBlockedPayload
import archives.tater.rpgskills.networking.SkillUpgradePayload
import io.wispforest.accessories.api.events.CanEquipCallback
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.fabric.api.util.TriState
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object RPGSkills : ModInitializer {
	const val MOD_ID = "rpgskills"

	@JvmStatic
	fun id(path: String): Identifier = Identifier.of(MOD_ID, path)

    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

	val CONFIG = RPGSkillsConfig.load()

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		RPGSkillsItems.register()

		DynamicRegistries.registerSynced(Skill.key, Skill.CODEC)
		DynamicRegistries.registerSynced(LockGroup.key, LockGroup.CODEC)
		DynamicRegistries.registerSynced(SkillClass.key, SkillClass.CODEC)

		CommandRegistrationCallback.EVENT.register(RPGSkillsCommands)

		ResourceManagerHelper.registerBuiltinResourcePack(
			id("test_pack"),
			FabricLoader.getInstance().getModContainer(MOD_ID).get(),
			if (FabricLoader.getInstance().isDevelopmentEnvironment) ResourcePackActivationType.DEFAULT_ENABLED else ResourcePackActivationType.NORMAL
		)

		PayloadTypeRegistry.playS2C().register(RecipeBlockedPayload.ID, RecipeBlockedPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(ChooseClassPayload.ID, ChooseClassPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(ClassChoicePayload.ID, ClassChoicePayload.CODEC)
		PayloadTypeRegistry.playC2S().register(SkillUpgradePayload.ID, SkillUpgradePayload.CODEC)

		SkillsComponent.registerEvents()

		registerLockEvents()

		RPGSkillsAttributes.register()
	}
}
