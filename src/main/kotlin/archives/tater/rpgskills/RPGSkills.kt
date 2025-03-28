package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.networking.SkillUpgradePacket
import archives.tater.rpgskills.util.SimpleSynchronousResourceReloadListener
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.registryOf
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resource.ResourceType
import net.minecraft.sound.SoundEvents
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
			if (FabricLoader.getInstance().isDevelopmentEnvironment) ResourcePackActivationType.DEFAULT_ENABLED else ResourcePackActivationType.NORMAL
		)

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(SimpleSynchronousResourceReloadListener(id("clear_locked_items")) {
			LockGroup.clearLocked()
		})

		ServerPlayNetworking.registerGlobalReceiver(SkillUpgradePacket.TYPE) { packet, player, respond ->
			val skillsComponent = player[SkillsComponent]
			val skill = registryOf(player, Skill).entryOf(packet.skill)
			if (skillsComponent.canUpgrade(skill)) {
				skillsComponent.remainingLevelPoints -= skillsComponent.getUpgradeCost(skill)!!
				skillsComponent[skill]++
				player.world.playSound(null, player.x, player.y, player.z, SoundEvents.ENTITY_PLAYER_LEVELUP, player.soundCategory, 1f, 1f)
			}
		}
	}
}
