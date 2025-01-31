package archives.tater.rpgskills

import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.util.*
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType.getPlayer
import net.minecraft.command.argument.EntityArgumentType.player
import net.minecraft.command.argument.RegistryEntryArgumentType.getRegistryEntry
import net.minecraft.command.argument.RegistryEntryArgumentType.registryEntry
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
                    argument("player", player()) {
                        argument("skill", registryEntry(registryAccess, Skill.key)) {
                            subExec("get") { command ->
                                val level = getPlayer(command, "player")[SkillsComponent][getRegistryEntry(command, "skill", Skill.key).key.get()]
                                command.source.sendFeedback(Text.literal(level.toString()), false)
                                level
                            }
                            sub("add") {
                                argumentExec("amount", integer()) { command ->
                                    getPlayer(command, "player")[SkillsComponent][getRegistryEntry(command, "skill", Skill.key).key.get()] += getInteger(command, "amount")
                                    1
                                }
                            }
                            sub("set") {
                                argumentExec("amount", integer(0)) { command ->
                                    getPlayer(command, "player")[SkillsComponent][getRegistryEntry(command, "skill", Skill.key).key.get()] = getInteger(command, "amount")
                                    1
                                }
                            }
                        }
                    }
                }
                sub("levelpoints") {
                    argument("player", player()) {
                        sub("set") {
                            argumentExec("amount", integer(0)) { command ->
                                getPlayer(command, "player")[SkillsComponent].remainingLevelPoints = getInteger(command, "amount")
                                1
                            }
                        }
                        sub("add") {
                            argumentExec("amount", integer()) { command ->
                                getPlayer(command, "player")[SkillsComponent].remainingLevelPoints += getInteger(command, "amount")
                                1
                            }
                        }
                    }
                }
            }
        }
    }
}
