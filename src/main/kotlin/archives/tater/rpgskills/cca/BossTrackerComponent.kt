package archives.tater.rpgskills.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.util.*
import archives.tater.rpgskills.util.get
import net.minecraft.command.argument.EntityArgumentType.player
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Formatting
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentAccess
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import kotlin.collections.iterator
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

class BossTrackerComponent(private val owner: ComponentAccess, private val server: MinecraftServer?) : Component, AutoSyncedComponent {
    private val _defeated = mutableSetOf<EntityType<*>>()
    val defeated: Set<EntityType<*>> get() = _defeated
    val defeatedCount get() = _defeated.size
    var minDefeated: Int = 0

    var maxLevel: Int = if (server == null) Int.MAX_VALUE else RPGSkills.CONFIG.baseLevelCap
        private set
    val hasCap get() = maxLevel < Int.MAX_VALUE

    val increasesLevelCap: RegistryEntryList<EntityType<*>> by lazy {
        server
            ?.registryManager[RegistryKeys.ENTITY_TYPE]
            ?.getEntryList(RPGSkillsTags.INCREASES_LEVEL_CAP)
            ?.getOrNull()
            ?: RegistryEntryList.empty()
    }
    val totalCount get() = increasesLevelCap.size()
    val requiredCount get() = RPGSkills.CONFIG.capRemoveBossCount.takeIf { it in 0..totalCount } ?: totalCount

    constructor(scoreboard: Scoreboard, server: MinecraftServer?) : this(scoreboard as ComponentAccess, server)
    constructor(team: Team, scoreboard: Scoreboard, server: MinecraftServer?) : this(team, server)

    fun hasDefeated(entity: LivingEntity) = entity.type in defeated

    private fun updateState() {
        updateLevelCap()
        updateMinDefeated(server!!)
    }

    private fun updateLevelCap() {
        maxLevel = if (defeatedCount >= requiredCount)
            Int.MAX_VALUE
        else
            RPGSkills.CONFIG.baseLevelCap + RPGSkills.CONFIG.levelCapIncreasePerBoss * defeatedCount
    }

    fun reset() {
        _defeated.clear()
        updateState()
        key.sync(owner)
    }

    fun add(entity: EntityType<*>): Boolean {
        if (!(entity isIn RPGSkillsTags.INCREASES_LEVEL_CAP)) return false
        if (!_defeated.add(entity)) return false
        updateState()
        key.sync(owner)
        return true
    }

    fun remove(entity: EntityType<*>): Boolean {
        if (!_defeated.remove(entity)) return false
        updateState()
        key.sync(owner)
        return true
    }

    override fun readFromNbt(
        tag: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup
    ) {
        CODEC.update(this, tag).logIfError()
        if (server != null)
            updateState()
    }

    override fun writeToNbt(
        tag: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup
    ) {
        CODEC.encode(this, tag).logIfError()
    }

    companion object : ComponentKeyHolder<BossTrackerComponent> {
        override val key: ComponentKey<BossTrackerComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("level_cap"), BossTrackerComponent::class.java)

        val CODEC = recordMutationCodec(
            Registries.ENTITY_TYPE.codec.mutateCollection().fieldFor("defeated_bosses", BossTrackerComponent::_defeated),
            intRangeCodec(min = 0).fieldOf("max_level").forAccess(BossTrackerComponent::maxLevel),
        )

        val BOSS_DEFEAT_TITLE = Translation.unit("rpgskills.title.boss_defeat") {
            formatted(Formatting.AQUA)
        }

        val BOSS_DEFEAT_MESSAGE = Translation.arg("rpgskills.announcement.boss_defeat") {
            formatted(Formatting.AQUA)
        }

        val BOSS_DEFEAT_TEAM_MESSAGE = Translation.arg("rpgskills.announcement.boss_defeat.team") {
            formatted(Formatting.AQUA)
        }

        val ENEMIES_STRENGTHEN_MESSAGE = Translation.unit("rpgskills.announcement.enemies_strengthen") {
            formatted(Formatting.AQUA)
        }

        val CAP_RAISE_MESSAGE = Translation.arg("rpgskills.announcement.levelcap.raised") {
            formatted(Formatting.AQUA)
        }
        val CAP_REMOVED_MESSAGE = Translation.arg("rpgskills.announcement.levelcap.removed") {
            formatted(Formatting.AQUA)
        }

        val CAP_RAISE_TEAM_MESSAGE = Translation.arg("rpgskills.announcement.levelcap.raised.team") {
            formatted(Formatting.AQUA)
        }
        val CAP_REMOVED_TEAM_MESSAGE = Translation.arg("rpgskills.announcement.levelcap.removed.team") {
            formatted(Formatting.AQUA)
        }

        val BOSS_DEFEAT_SCALING = RPGSkills.id("bosses_defeated_bonus")

        private fun announcement(subject: ComponentAccess, unteamedMessage: ArgTranslation, teamMessage: ArgTranslation, vararg otherArgs: Any) =
            when (subject) {
                is Team -> teamMessage.text(subject.displayName, *otherArgs)
                else -> unteamedMessage.text(*otherArgs)
            }

        @JvmStatic
        fun onDeath(entity: LivingEntity) {
            val server = entity.server ?: return
            if (!(entity.type isIn RPGSkillsTags.INCREASES_LEVEL_CAP)) return
            if (entity !is MobEntity) return
            val playerManager = server.playerManager

            for (player in entity[DefeatSourceComponent].getRewarded())
                player.networkHandler.sendPacket(TitleS2CPacket(BOSS_DEFEAT_TITLE.text))

            val teams = entity[DefeatSourceComponent].getRewarded().map { it.scoreboardTeam ?: it.scoreboard }.toSet()

            for (team in teams) team[BossTrackerComponent].apply {
                if (hasDefeated(entity)) continue
                _defeated.add(entity.type)
                val hadCap = hasCap
                updateLevelCap()
                key.sync(owner)

                playerManager.broadcast(announcement(team, BOSS_DEFEAT_MESSAGE, BOSS_DEFEAT_TEAM_MESSAGE, entity.type.name), false)
                when {
                    !hadCap -> {}
                    hasCap -> playerManager.broadcast(announcement(team, CAP_RAISE_MESSAGE, CAP_RAISE_TEAM_MESSAGE, maxLevel), false)
                    else -> playerManager.broadcast(announcement(team, CAP_REMOVED_MESSAGE, CAP_REMOVED_TEAM_MESSAGE), false)
                }
            }

            if (updateMinDefeated(server))
                playerManager.broadcast(ENEMIES_STRENGTHEN_MESSAGE.text, false)
        }

        fun updateMinDefeated(server: MinecraftServer) = with (server.scoreboard[BossTrackerComponent]) {
            val newMinDefeated = (server.scoreboard.teams.minOfOrNull { it[BossTrackerComponent].defeatedCount } ?: 0).let {
                if (RPGSkills.CONFIG.attributeIncreasesIncludeUnteamed) min(defeatedCount, it) else it
            }
            val increased = newMinDefeated > minDefeated
            minDefeated = newMinDefeated
            key.sync(owner)
            increased
        }

        @JvmStatic
        fun applyBuffs(entity: LivingEntity) {
            if (entity !is Monster) return

            val server = entity.server ?: return
            val defeated = server.scoreboard[BossTrackerComponent].minDefeated
            if (defeated <= 0) return

            if (!(entity isIn RPGSkillsTags.BOSS_ATTRIBUTE_AFFECTED))
                for ((attribute, modifier) in RPGSkills.CONFIG.attributeIncreases)
                    entity.getAttributeInstance(attribute)?.addPersistentModifier(modifier.build(BOSS_DEFEAT_SCALING, defeated.toDouble()))

            if (entity.health < entity.maxHealth)
                entity.health = entity.maxHealth
        }

        val PlayerEntity.bossTracker get() = (scoreboardTeam ?: scoreboard)[BossTrackerComponent]
        val PlayerEntity.bossAssist get() = (RPGSkills.CONFIG.maxBossAssist - bossTracker.defeatedCount).coerceAtLeast(0)

        @JvmStatic
        fun modifyDamageTaken(player: PlayerEntity, damage: Float) =
            damage / (1 + player.bossAssist * RPGSkills.CONFIG.damageTakenDivisorPerBossAssist)

        @JvmStatic
        fun modifyDamageDealt(player: PlayerEntity, damage: Float) =
            damage * (1 + player.bossAssist * RPGSkills.CONFIG.damageDealtMultiplierPerBossAssist)
    }
}