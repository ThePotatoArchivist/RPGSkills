package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.networking.RecipeBlockedPayload
import archives.tater.rpgskills.networking.SkillUpgradePayload
import archives.tater.rpgskills.util.get
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.player.*
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object RPGSkills : ModInitializer {
	const val MOD_ID = "rpgskills"

	@JvmStatic
	fun id(path: String): Identifier = Identifier.of(MOD_ID, path)

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

		PayloadTypeRegistry.playS2C().register(RecipeBlockedPayload.ID, RecipeBlockedPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(SkillUpgradePayload.ID, SkillUpgradePayload.CODEC)

		ServerPlayNetworking.registerGlobalReceiver(SkillUpgradePayload.ID) { payload, context ->
			val player = context.player()
			val skillsComponent = player[SkillsComponent]
			val skill = payload.skill
			if (skillsComponent.canUpgrade(skill)) {
				skillsComponent.remainingLevelPoints -= skillsComponent.getUpgradeCost(skill)!!
				skillsComponent[skill]++
				player.world.playSound(
					null,
					player.x,
					player.y,
					player.z,
					SoundEvents.ENTITY_PLAYER_LEVELUP,
					player.soundCategory,
					1f,
					1f
				)
			}
		}

		UseItemCallback.EVENT.register { player, _, hand ->
			val stack = player.getStackInHand(hand)
			val lockGroup = LockGroup.findLocked(player, stack)
			if (lockGroup == null)
				TypedActionResult.pass(stack)
			else {
				player.sendMessage(lockGroup.itemMessage(), true)
				TypedActionResult.fail(stack)
			}
		}

		UseEntityCallback.EVENT.register { player, _, _, entity, _ ->
			val lockGroup = LockGroup.findLocked(player, entity)
			if (lockGroup == null)
				ActionResult.PASS
			else {
				player.sendMessage(lockGroup.entityMessage(), true)
				ActionResult.FAIL
			}
		}

		UseBlockCallback.EVENT.register { player, world, hand, hit ->
			val blockLockGroup = LockGroup.findLocked(player, world.getBlockState(hit.blockPos))
			if (blockLockGroup != null) {
				player.sendMessage(blockLockGroup.blockMessage(), true)
				return@register ActionResult.FAIL
			}
			val itemLockGroup = LockGroup.findLocked(player, player.getStackInHand(hand))
			if (itemLockGroup == null)
				ActionResult.PASS
			else {
				player.sendMessage(itemLockGroup.itemMessage(), true)
				ActionResult.FAIL
			}
		}

		AttackBlockCallback.EVENT.register { player, world, hand, _, _ ->
			val lockGroup = LockGroup.findLocked(player, player.getStackInHand(hand))
			if (lockGroup == null)
				ActionResult.PASS
			else {
				player.sendMessage(lockGroup.itemMessage(), true)
				ActionResult.FAIL
			}
		}

		AttackEntityCallback.EVENT.register { player, _, hand, _, _ ->
			val lockGroup = LockGroup.findLocked(player, player.getStackInHand(hand))
			if (lockGroup == null)
				ActionResult.PASS
			else {
				player.sendMessage(lockGroup.itemMessage(), true)
				ActionResult.FAIL
			}
		}
	}
}
