package archives.tater.rpgskills

import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
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

object RPGSkillsCommands : CommandRegistrationCallback {
    object Translations {
        const val LIST_NONE = "commands.$MOD_ID.skills.list.none"
        const val LIST = "commands.$MOD_ID.skills.list"
        const val GET_LEVEL = "commands.$MOD_ID.skills.level.get"
        const val ADD_LEVEL = "commands.$MOD_ID.skills.level.add"
        const val SET_LEVEL = "commands.$MOD_ID.skills.level.set"
        const val SET_POINTS = "commands.$MOD_ID.skills.levelpoints.set"
        const val ADD_POINTS = "commands.$MOD_ID.skills.levelpoints.add"
    }

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        dispatcher.apply {
            command("skills") {
                subExec("list") { command ->
                    val skills = command.source.server.registryManager[Skill].indexedEntries

                    if (skills.size() == 0)
                        command.source.sendFeedback(Text.translatable(Translations.LIST_NONE), false)
                    else
                        command.source.sendFeedback(Text.translatable(Translations.LIST, skills.size(), Text.empty().apply {
                            skills.forEachIndexed { index, entry ->
                                if (index > 0) append(Text.literal(",  "))
                                append(entry.name)
                            }
                        }), false)

                    skills.size()
                }
                sub("level") {
                    argument("player", player()) {
                        argument("skill", registryEntry(registryAccess, Skill.key)) {
                            subExec("get") { command ->
                                val player = getPlayer(command, "player")
                                val skill = getRegistryEntry(command, "skill", Skill.key)
                                val level = player[SkillsComponent][skill.key.get()]

                                command.source.sendFeedback(Text.translatable(Translations.GET_LEVEL, player.displayName, skill.name, level), false)
                                level
                            }
                            sub("add") {
                                argumentExec("amount", integer()) { command ->
                                    val player = getPlayer(command, "player")
                                    val amount = getInteger(command, "amount")
                                    val skill = getRegistryEntry(command, "skill", Skill.key)

                                    player[SkillsComponent][skill.key.get()] += amount

                                    command.source.sendFeedback(Text.translatable(Translations.ADD_LEVEL, player.displayName, skill.name, amount), true)
                                    amount
                                }
                            }
                            sub("set") {
                                argumentExec("amount", integer(0)) { command ->
                                    val player = getPlayer(command, "player")
                                    val amount = getInteger(command, "amount")
                                    val skill = getRegistryEntry(command, "skill", Skill.key)

                                    player[SkillsComponent][skill.key.get()] = amount

                                    command.source.sendFeedback(Text.translatable(Translations.SET_LEVEL, player.displayName, skill.name, amount), true)
                                    amount
                                }
                            }
                        }
                    }
                }
                sub("levelpoints") {
                    argument("player", player()) {
                        sub("set") {
                            argumentExec("amount", integer(0)) { command ->
                                val player = getPlayer(command, "player")
                                val amount = getInteger(command, "amount")

                                player[SkillsComponent].remainingLevelPoints = amount

                                command.source.sendFeedback(Text.translatable(Translations.SET_POINTS, player.displayName, amount), true)
                                amount
                            }
                        }
                        sub("add") {
                            argumentExec("amount", integer()) { command ->
                                val player = getPlayer(command, "player")
                                val amount = getInteger(command, "amount")

                                player[SkillsComponent].remainingLevelPoints += amount

                                command.source.sendFeedback(Text.translatable(Translations.ADD_POINTS, player.displayName, amount), true)
                                amount
                            }
                        }
                    }
                }
            }
        }
    }
}
