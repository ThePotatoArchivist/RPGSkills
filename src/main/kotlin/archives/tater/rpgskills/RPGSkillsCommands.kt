package archives.tater.rpgskills

import archives.tater.rpgskills.RPGSkills.MOD_ID
import archives.tater.rpgskills.cca.BossTrackerComponent
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.Companion.name
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.util.*
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType.*
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType.getRegistryEntry
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType.registryEntry
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object RPGSkillsCommands : CommandRegistrationCallback {
    val LIST_SKILLS_NONE = Translation.unit("commands.$MOD_ID.skills.list.none")
    val LIST_SKILLS = Translation.arg("commands.$MOD_ID.skills.list")
    val GET_LEVEL = Translation.arg("commands.$MOD_ID.skills.level.get")
    val ADD_LEVEL = Translation.arg("commands.$MOD_ID.skills.level.add")
    val SET_LEVEL = Translation.arg("commands.$MOD_ID.skills.level.set")
    val SET_POINTS = Translation.arg("commands.$MOD_ID.skills.levelpoints.set")
    val ADD_POINTS = Translation.arg("commands.$MOD_ID.skills.levelpoints.add")
    val GET_POINTS = Translation.arg("commands.$MOD_ID.skills.levelpoints.get")
    val RESET_CLASS = Translation.arg("commands.$MOD_ID.skills.class.reset")
    val SET_CLASS = Translation.arg("commands.$MOD_ID.skills.class.set")
    val LIST_BOSSES = Translation.arg("commands.$MOD_ID.skills.bosses.list")
    val LIST_BOSSES_ALL = Translation.arg("commands.$MOD_ID.skills.bosses.list.all")
    val ADD_BOSS = Translation.arg("commands.$MOD_ID.skills.bosses.add")
    val REMOVE_BOSS = Translation.arg("commands.$MOD_ID.skills.bosses.remove")
    val INVALID_BOSS = Translation.arg("commands.$MOD_ID.skills.bosses.invalid")
    val RESET_BOSSES = Translation.unit("commands.$MOD_ID.skills.bosses.reset")
    val DESPAWN_SINGLE = Translation.arg("commands.$MOD_ID.despawn.single")
    val DESPAWN_MULTIPLE = Translation.arg("commands.$MOD_ID.despawn.multiple")

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        with (dispatcher) {
            command("skills") {
                requires { it.hasPermissionLevel(2) }
                subExec("list") { command ->
                    val skills = command.source.registryManager[Skill].streamEntriesOrdered(RPGSkillsTags.SKILL_ORDER).toList()

                    if (skills.isEmpty())
                        command.source.sendFeedback(LIST_SKILLS_NONE.text, false)
                    else
                        command.source.sendFeedback(LIST_SKILLS.text(skills.size, skills.joinToText { it.name }), false)

                    skills.size
                }
                sub("skilllevel") {
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
                sub("points") {
                    argument("player", player()) {
                        sub("set") {
                            argumentExec("amount", integer(0)) { command ->
                                val player = getPlayer(command, "player")
                                val amount = getInteger(command, "amount")

                                player[SkillsComponent].points = amount

                                command.source.sendFeedback(SET_POINTS.text(player.displayName!!, amount), true)
                                amount
                            }
                        }
                        sub("add") {
                            argumentExec("amount", integer()) { command ->
                                val player = getPlayer(command, "player")
                                val amount = getInteger(command, "amount")

                                player[SkillsComponent].points += amount

                                command.source.sendFeedback(ADD_POINTS.text(player.displayName!!, amount), true)
                                amount
                            }
                        }
                    }
                }
                sub("spendablelevels") {
                    argument("player", player()) {
                        sub("set") {
                            argumentExec("amount", integer(0)) { command ->
                                val player = getPlayer(command, "player")
                                val amount = getInteger(command, "amount")

                                player[SkillsComponent].spendableLevels = amount

                                command.source.sendFeedback(SET_POINTS.text(player.displayName!!, amount), true)
                                amount
                            }
                        }
                        sub("add") {
                            argumentExec("amount", integer()) { command ->
                                val player = getPlayer(command, "player")
                                val amount = getInteger(command, "amount")

                                player[SkillsComponent].spendableLevels += amount

                                command.source.sendFeedback(ADD_POINTS.text(player.displayName!!, amount), true)
                                amount
                            }
                        }
                    }
                }
                sub("class") {
                    sub("reset") {
                        argumentExec("player", player()) { command ->
                            val player = getPlayer(command, "player")
                            player[SkillsComponent].skillClass = null
                            SkillsComponent.openClassScreen(player)
                            command.source.sendFeedback(RESET_CLASS.text(player.displayName!!), true)
                            0
                        }
                    }
                    sub("set") {
                        argument("player", player()) {
                            argumentExec("class", registryEntry(registryAccess, SkillClass.key)) { command ->
                                val player = getPlayer(command, "player")
                                val skillClass = getRegistryEntry(command, "class", SkillClass.key)
                                player[SkillsComponent].skillClass = skillClass
                                command.source.sendFeedback(SET_CLASS.text(player.displayName!!, skillClass.value.name), true)
                                0
                            }
                        }
                    }
                }
                sub("bosses") {
                    sub("list") {
                        executes { command ->
                            val component = command.source.server.overworld[BossTrackerComponent]
                            val defeated = component.defeated
                            command.source.sendFeedback(LIST_BOSSES.text(component.defeatedCount, component.totalCount, defeated.joinToText { it.name }), false)
                            defeated.size
                        }
                        subExec("all") { command ->
                            val component = command.source.server.overworld[BossTrackerComponent]
                            val all = component.increasesLevelCap.toList()
                            command.source.sendFeedback(LIST_BOSSES_ALL.text(component.totalCount, all.joinToText { it.value.name }), false)
                            all.size
                        }
                    }
                    subExec("reset") {
                        BossTrackerComponent.update(it.source.server) {
                            reset()
                            true
                        }
                        it.source.sendFeedback(RESET_BOSSES.text, false)
                        0
                    }
                    sub("add") {
                        argumentExec("entity", registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE)) {
                            val entity = getRegistryEntry(it, "entity", RegistryKeys.ENTITY_TYPE)
                            if (!(entity.value isIn RPGSkillsTags.INCREASES_LEVEL_CAP)) {
                                it.source.sendError(INVALID_BOSS.text(entity.value.name))
                                return@argumentExec 0
                            }
                            BossTrackerComponent.update(it.source.server) {
                                add(entity.value)
                            }
                            it.source.sendFeedback(ADD_BOSS.text(entity.value.name), true)
                            1
                        }
                    }
                    sub("remove") {
                        argumentExec("entity", registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE)) {
                            val entity = getRegistryEntry(it, "entity", RegistryKeys.ENTITY_TYPE)
                            if (!(entity.value isIn RPGSkillsTags.INCREASES_LEVEL_CAP)) {
                                it.source.sendError(INVALID_BOSS.text(entity.value.name))
                                return@argumentExec 0
                            }
                            BossTrackerComponent.update(it.source.server) {
                                remove(entity.value)
                            }
                            it.source.sendFeedback(REMOVE_BOSS.text(entity.value.name), true)
                            1
                        }
                    }
                }
            }

            command("despawn") {
                requires { it.hasPermissionLevel(2) }
                argumentExec("targets", entities()) {
                    val targets = getEntities(it, "targets")
                    for (entity in targets)
                        entity.discard()

                    if (targets.size == 1)
                        it.source.sendFeedback(DESPAWN_SINGLE.text(targets.first().displayName!!), true)
                    else
                        it.source.sendFeedback(DESPAWN_MULTIPLE.text(targets.size), true)

                    targets.size
                }
            }
        }
    }
}
