package archives.tater.rpgskills

import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.*
import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Util.createTranslationKey

object RPGSkillsCommands : CommandRegistrationCallback {
    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        dispatcher.apply {
            command("skills") {
                sub("list") {
                    argument("player", EntityArgumentType.player()) {
                        executes { command ->
                            command.source.sendFeedback(Text.literal("Skills: ").apply {
                                for (entry in command.source.server.registryManager[Skill].indexedEntries) {
                                    append(createTranslationKey("skill", entry.key.get().value))
                                    append(entry.value.levels[1].unlocks.items.getOrNull(0)?.translationKey)
                                    append(Text.literal(",  "))
                                }
                            }, false)
                            1
                        }
                    }
                }
            }
        }
    }
}
