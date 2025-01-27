package archives.tater.rpgskills

import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.util.*
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.RegistryEntryArgumentType
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
                subExec("list") { command ->
                    command.source.sendFeedback(Text.literal("Skills: ").apply {
                        command.source.server.registryManager[Skill].indexedEntries.forEachIndexed { index, entry ->
                            if (index > 0) append(Text.literal(",  "))
                            append(Text.translatable(createTranslationKey("skill", entry.key.get().value)))
                        }
                    }, false)
                    1
                }
                sub("level") {
                    argument("player", EntityArgumentType.player()) {
                        argument("skill", RegistryEntryArgumentType.registryEntry(registryAccess, Skill.key)) {
                            subExec("get") { command ->
                                val level = EntityArgumentType.getPlayer(command, "player")[SkillsComponent][RegistryEntryArgumentType.getRegistryEntry(command, "skill", Skill.key)]
                                command.source.sendFeedback(Text.literal(level.toString()), false)
                                level
                            }
                            sub("add") {
                                argumentExec("amount", IntegerArgumentType.integer(0)) { command ->
                                    EntityArgumentType.getPlayer(
                                        command,
                                        "player"
                                    )[SkillsComponent][RegistryEntryArgumentType.getRegistryEntry(
                                        command,
                                        "skill",
                                        Skill.key
                                    )] += IntegerArgumentType.getInteger(command, "amount")
                                    1
                                }
                            }
                            sub("set") {
                                argumentExec("amount", IntegerArgumentType.integer(0)) { command ->
                                    EntityArgumentType.getPlayer(command, "player")[SkillsComponent][RegistryEntryArgumentType.getRegistryEntry(command, "skill", Skill.key)] = IntegerArgumentType.getInteger(command, "amount")
                                    1
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
