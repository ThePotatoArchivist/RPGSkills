package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.PassiveJob
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.data.cca.SkillsComponent.JobEntry
import archives.tater.rpgskills.entity.SkillPointOrbEntity
import archives.tater.rpgskills.networking.ChooseClassPayload
import archives.tater.rpgskills.networking.ClassChoicePayload
import archives.tater.rpgskills.networking.SkillPointIncreasePayload
import archives.tater.rpgskills.networking.SkillUpgradePayload
import archives.tater.rpgskills.util.*
import com.google.common.collect.HashMultimap
import com.mojang.serialization.Codec
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryCodecs
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryElementCodec
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryFixedCodec
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent
import org.ladysnake.cca.api.v3.entity.RespawnableComponent
import java.util.function.Predicate

@Suppress("UnstableApiUsage")
class SkillsComponent(private val player: PlayerEntity) : RespawnableComponent<SkillsComponent>, AutoSyncedComponent, ServerTickingComponent {
    private var _skillClass: RegistryEntry<SkillClass>? = null
    var skillClass by ::_skillClass.synced(key, player)

    private var _skills = mutableMapOf<RegistryEntry<Skill>, Int>()
    val skills: Map<RegistryEntry<Skill>, Int>
        get() = _skills

    var level = 0
        private set // managed by _points

    private var _points = 0
        set(value) {
            field = value.coerceAtMost(MAX_POINTS)
            level = getLevelForPoints(value)
        }
    var points by ::_points.synced(key, player)

    private var spentLevels = 0
    var spendableLevels
        get() = level - spentLevels
        set(value) {
            spentLevels = level - value
            key.sync(player)
        }

    private var jobCooldowns = mutableMapOf<JobEntry, Int>()

    val levelProgress get() = getRemainingPoints(points) / getPointsForNextLevel(level).toFloat()

    val isPointsFull get() = level >= MAX_LEVEL

    private var modifiers: HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> =
        HashMultimap.create()

    operator fun get(skill: RegistryEntry<Skill>) = _skills.getOrDefault(skill, 0)
    operator fun set(skill: RegistryEntry<Skill>, level: Int) {
        _skills[skill] = level.coerceIn(0, skill.value.levels.size)
        updateAttributes()
        updateJobs()
        key.sync(player)
    }

    fun getUpgradeCost(skill: RegistryEntry<Skill>): Int? = skill.value.levels
        .getOrNull(this[skill])?.cost

    fun canUpgrade(skill: RegistryEntry<Skill>): Boolean = getUpgradeCost(skill)
        ?.let { spendableLevels >= it } ?: false

    fun resetSkillsToClass() {
        _skills.clear()
        skillClass?.value?.startingLevels?.let(_skills::putAll)
        spentLevels = 0
        updateAttributes()
        key.sync(player)
    }

    private fun getAttributeModifiers(): HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> =
        HashMultimap.create<RegistryEntry<EntityAttribute>, EntityAttributeModifier>().apply {
            for ((skill, playerLevel) in _skills)
                skill.value.levels
                    .filter { playerLevel >= it.cost }
                    .forEachIndexed { levelIndex, level ->
                        for ((attribute, modifier) in level.attributes)
                            put(
                                attribute,
                                modifier.build(skill.key.orElseThrow().value.withPath { "skill/$it/$levelIndex" })
                            )
                    }
        }

    private fun updateAttributes() {
        if (player.world.isClient) return
        player.attributes.removeModifiers(modifiers)
        modifiers = getAttributeModifiers().also {
            player.attributes.addTemporaryModifiers(it)
        }
    }

    private fun updateJobs() {
        if (player.world.isClient) return
        val newJobCooldowns = mutableMapOf<JobEntry, Int>()
        for ((skill, maxLevel) in _skills)
            for (levelIndex in 0..<maxLevel) {
                val level = skill.value.levels[levelIndex]
                for (job in level.jobs) {
                    val entry = JobEntry(job, skill, level)
                    newJobCooldowns[entry] = jobCooldowns[entry] ?: 0
                }
            }
        jobCooldowns = newJobCooldowns
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: AbstractCriterion.Conditions> onCriterion(criterion: Criterion<T>, condition: Predicate<T>) {
        val player = player as? ServerPlayerEntity ?: return

        for ((jobEntry, cooldown) in jobCooldowns) {
            if (cooldown > 0) continue
            val job = jobEntry.job
            if (job.criteria.trigger == criterion && condition.test(job.criteria.conditions as T)) {
                if (job.spawnAsOrbs) {
                    SkillPointOrbEntity.spawnOrbs(player.serverWorld, player, player.pos, job.rewardPoints)
                } else {
                    points += job.rewardPoints
                    ServerPlayNetworking.send(player, SkillPointIncreasePayload)
                }
                jobCooldowns[jobEntry] = job.cooldownTicks
            }
        }
    }

    override fun serverTick() {
        for ((job, cooldown) in jobCooldowns)
            if (cooldown > 0)
                jobCooldowns[job] = cooldown - 1
    }

    override fun shouldCopyForRespawn(lossless: Boolean, keepInventory: Boolean, sameCharacter: Boolean): Boolean =
        sameCharacter

    override fun copyFrom(other: SkillsComponent, registryLookup: RegistryWrapper.WrapperLookup) {
        _skillClass = other._skillClass
        _skills = other._skills
        _points = other.points
        spentLevels = other.spentLevels
        jobCooldowns = other.jobCooldowns
        updateAttributes()
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.update(this, tag, registryLookup).logIfError()
        updateAttributes()
        updateJobs()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, tag, registryLookup).logIfError()
    }

    @ConsistentCopyVisibility
    data class JobEntry private constructor(
        val job: PassiveJob,
        val skill: RegistryEntry<Skill>,
        val level: Int,
        val index: Int,
    ) {
        private constructor(skill: RegistryEntry<Skill>, level: Int, index: Int) :
                this(skill.value.levels[level].jobs[index], skill, level, index)

        constructor(job: PassiveJob, skill: RegistryEntry<Skill>, level: Skill.Level) :
                this(job, skill, skill.value.levels.indexOf(level), level.jobs.indexOf(job))

        companion object {
            val CODEC: Codec<JobEntry> = RecordCodecBuilder.create { it.group(
                RegistryFixedCodec.of(Skill.key).fieldOf("skill").forGetter(JobEntry::skill),
                intRangeCodec(min = 0).fieldOf("level").forGetter(JobEntry::level),
                intRangeCodec(min = 0).fieldOf("index").forGetter(JobEntry::index)
            ).apply(it, ::JobEntry) }
        }
    }

    companion object : ComponentKeyHolder<SkillsComponent, PlayerEntity> {

        val CODEC = recordMutationCodec(
            RegistryFixedCodec.of(SkillClass.key).optionalFieldOf("class").forAccess(SkillsComponent::_skillClass),
            Codec.unboundedMap(RegistryFixedCodec.of(Skill.key), Codec.INT).mutate().fieldFor("skills", SkillsComponent::_skills),
            Codec.INT.fieldOf("spent").forAccess(SkillsComponent::spentLevels),
            Codec.INT.fieldOf("points").forAccess(SkillsComponent::_points),
            Codec.unboundedMap(JobEntry.CODEC, Codec.INT).mutate().fieldFor("job_cooldowns", SkillsComponent::jobCooldowns),
        )

        @JvmField
        val KEY: ComponentKey<SkillsComponent> = ComponentRegistry.getOrCreate(RPGSkills.id("skills"), SkillsComponent::class.java)

        override val key: ComponentKey<SkillsComponent> get() = KEY

        const val MAX_LEVEL = 50

        private val LEVEL_REQUIREMENTS = (0..<MAX_LEVEL)
            .runningFold(0) { acc, lvl -> acc + getPointsForNextLevel(lvl) }
        private val LEVEL_REQUIREMENTS_REVERSED = LEVEL_REQUIREMENTS.withIndex().reversed()

        val MAX_POINTS = LEVEL_REQUIREMENTS[MAX_LEVEL]

        /**
         * Matches vanilla XP
         * @return The number of points needed to get to `currentLevel + 1` from `currentLevel`
         */
        fun getPointsForNextLevel(currentLevel: Int) = when {
            currentLevel < 16 -> 2 * currentLevel + 7
            currentLevel < 31 -> 5 * currentLevel - 28
            else -> 9 * currentLevel - 158
        }

        fun getLevelForPoints(points: Int): Int =
            LEVEL_REQUIREMENTS_REVERSED.firstNotNullOfOrNull { (level, required) -> level.takeIf { required <= points } }
                ?: 0

        fun getRemainingPoints(points: Int) =
            points - (LEVEL_REQUIREMENTS_REVERSED.firstOrNull { (_, required) -> required <= points }?.value ?: 0)

        fun registerEvents() {
            // Choose class
            ServerPlayNetworking.registerGlobalReceiver(ClassChoicePayload.ID) { payload, context ->
                val player = context.player()
                val skillsComponent = player[SkillsComponent]
                if (skillsComponent.skillClass != null) {
                    RPGSkills.logger.warn("${player.name.string} tried to set skills class when it was already set")
                    return@registerGlobalReceiver
                }
                skillsComponent.skillClass = payload.skillClass
                skillsComponent.resetSkillsToClass()

                player.interactionManager.gameMode.setAbilities(player.abilities)
                player.sendAbilitiesUpdate()
            }

            // Upgrade skill
            ServerPlayNetworking.registerGlobalReceiver(SkillUpgradePayload.ID) { payload, context ->
                val player = context.player()
                val skillsComponent = player[SkillsComponent]
                val skill = payload.skill
                if (skillsComponent.canUpgrade(skill)) {
                    skillsComponent.spendableLevels -= skillsComponent.getUpgradeCost(skill)!!
                    skillsComponent[skill]++
                    player.world.playSoundFromEntity(
                        null, player,
                        SoundEvents.ENTITY_PLAYER_LEVELUP, player.soundCategory, 1f, 1f
                    )
                }
            }

            // Join without class
            ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
                val player = handler.player
                if (player[SkillsComponent].skillClass != null || server.registryManager[SkillClass].isEmpty()) return@register
                sender.sendPacket(ChooseClassPayload)
                player.abilities.invulnerable = true
                player.sendAbilitiesUpdate()
            }
        }
    }
}

