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
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType.getRegistryEntry
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType.registryEntry
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object RPGSkillsCommands : CommandRegistrationCallback {
    val LIST_NONE = Translation.unit("commands.$MOD_ID.skills.list.none")
    val LIST = Translation.arg("commands.$MOD_ID.skills.list")
    val GET_LEVEL = Translation.arg("commands.$MOD_ID.skills.level.get")
    val ADD_LEVEL = Translation.arg("commands.$MOD_ID.skills.level.add")
    val SET_LEVEL = Translation.arg("commands.$MOD_ID.skills.level.set")
    val SET_POINTS = Translation.arg("commands.$MOD_ID.skills.levelpoints.set")
    val ADD_POINTS = Translation.arg("commands.$MOD_ID.skills.levelpoints.add")

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        dispatcher.apply {
            command("skills") {
                subExec("list") { command ->
                    val skills = command.source.registryManager[Skill].streamEntries().toList()

                    if (skills.size == 0)
                        command.source.sendFeedback(LIST_NONE.text, false)
                    else
                        command.source.sendFeedback(LIST.text(skills.size, Text.empty().apply {
                            skills.forEachIndexed { index, entry ->
                                if (index > 0) append(Text.literal(", "))
                                append(entry.name)
                            }
                        }), false)

                    skills.size
                }
                sub("level") {
                    argument("player", player()) {
                        argument("skill", registryEntry(registryAccess, Skill.key)) {
                            subExec("get") { command ->
                                val player = getPlayer(command, "player")
                                val skill = getRegistryEntry(command, "skill", Skill.key)
                                val level = player[SkillsComponent][skill]

                                command.source.sendFeedback(GET_LEVEL.text(player.displayName!!, skill.name, level), false)
                                level
                            }
                            sub("add") {
                                argumentExec("amount", integer()) { command ->
                                    val player = getPlayer(command, "player")
                                    val amount = getInteger(command, "amount")
                                    val skill = getRegistryEntry(command, "skill", Skill.key)

                                    player[SkillsComponent][skill] += amount

                                    command.source.sendFeedback(ADD_LEVEL.text(player.displayName!!, skill.name, amount), true)
                                    amount
                                }
                            }
                            sub("set") {
                                argumentExec("amount", integer(0)) { command ->
                                    val player = getPlayer(command, "player")
                                    val amount = getInteger(command, "amount")
                                    val skill = getRegistryEntry(command, "skill", Skill.key)

                                    player[SkillsComponent][skill] = amount

                                    command.source.sendFeedback(SET_LEVEL.text(player.displayName!!, skill.name, amount), true)
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

                                player[SkillsComponent].skillLevels = amount

                                command.source.sendFeedback(SET_POINTS.text(player.displayName!!, amount), true)
                                amount
                            }
                        }
                        sub("add") {
                            argumentExec("amount", integer()) { command ->
                                val player = getPlayer(command, "player")
                                val amount = getInteger(command, "amount")

                                player[SkillsComponent].skillLevels += amount

                                command.source.sendFeedback(ADD_POINTS.text(player.displayName!!, amount), true)
                                amount
                            }
                        }
                    }
                }
            }
        }
    }
}
