package archives.tater.rpgskills

import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.client.gui.JobCompletedToast
import archives.tater.rpgskills.client.gui.screen.ClassScreen
import archives.tater.rpgskills.client.gui.screen.JobsScreen
import archives.tater.rpgskills.client.gui.screen.SkillsScreen
import archives.tater.rpgskills.client.render.SkillBarRenderer
import archives.tater.rpgskills.client.render.entity.SkillPointOrbEntityRenderer
import archives.tater.rpgskills.client.util.wasPressed
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.entity.RPGSkillsEntities
import archives.tater.rpgskills.networking.*
import archives.tater.rpgskills.util.RegistryCache
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.isEmpty
import archives.tater.rpgskills.util.value
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.sound.SoundEvents
import org.lwjgl.glfw.GLFW
import kotlin.jvm.optionals.getOrNull

object RPGSkillsClient : ClientModInitializer {
	@JvmField
	var blockedRecipeGroup: LockGroup? = null

	const val RPG_SKILLS_CATEGORY = "category.$MOD_ID.$MOD_ID"
	const val SKILLS_KEY_TRANSLATION = "key.$MOD_ID.screen.skills"
    const val JOBS_KEY_TRANSLATION = "key.$MOD_ID.screen.jobs"

	val skillsKey: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
		SKILLS_KEY_TRANSLATION,
		InputUtil.Type.KEYSYM,
		GLFW.GLFW_KEY_O,
		RPG_SKILLS_CATEGORY,
	))

    val jobsKey: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
        JOBS_KEY_TRANSLATION,
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_J,
        RPG_SKILLS_CATEGORY,
    ))

    private val JOB_SKILL_CACHE = RegistryCache(Skill.key) { skill -> skill.value.levels.flatMap { level -> level.jobs } }

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityRendererRegistry.register(RPGSkillsEntities.SKILL_POINT_ORB, ::SkillPointOrbEntityRenderer)

		SkillBarRenderer.register()

		ClientPlayNetworking.registerGlobalReceiver(RecipeBlockedPayload.ID) { payload, _ ->
			blockedRecipeGroup = payload.lockGroup.getOrNull()
		}

		ClientPlayNetworking.registerGlobalReceiver(ChooseClassPayload.id) { _, context ->
			val player = context.player()
			if (player[SkillsComponent].skillClass == null && !player.registryManager[SkillClass].isEmpty())
				context.client().setScreen(ClassScreen(player))
		}

        ClientPlayNetworking.registerGlobalReceiver(SkillPointIncreasePayload.id, SkillBarRenderer)

        ClientPlayNetworking.registerGlobalReceiver(JobCompletedPayload.ID) { payload, context ->
            val player = context.player()
            if (!player[SkillsComponent].isPointsFull)
                player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, (player.random.nextFloat() - player.random.nextFloat()) * 0.35F + 0.9F)
            context.client().toastManager.add(JobCompletedToast(
                payload.job.value,
                JOB_SKILL_CACHE[player.registryManager][payload.job]?.value
            ))
        }

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (skillsKey.wasPressed)
				client.player?.let {
					client.setScreen(SkillsScreen(it))
				}
            if (jobsKey.wasPressed)
                client.player?.let {
                    ClientPlayNetworking.send(RequestSkillSyncPayload)
                    client.setScreen(JobsScreen(it))
                }
		}

		ScreenEvents.BEFORE_INIT.register { _, _, _, _ ->
			blockedRecipeGroup = null
		}

		ItemTooltipCallback.EVENT.register { stack, _, _, tooltip ->
			ItemLockTooltip.appendTooltip(stack, MinecraftClient.getInstance().player, tooltip)
		}
	}
}
