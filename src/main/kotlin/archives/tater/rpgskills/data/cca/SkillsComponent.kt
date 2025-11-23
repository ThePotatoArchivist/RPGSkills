package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.entity.SkillPointOrbEntity
import archives.tater.rpgskills.networking.ChooseClassPayload
import archives.tater.rpgskills.networking.ClassChoicePayload
import archives.tater.rpgskills.networking.SkillPointIncreasePayload
import archives.tater.rpgskills.networking.SkillUpgradePayload
import archives.tater.rpgskills.util.*
import com.google.common.collect.HashMultimap
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryFixedCodec
import net.minecraft.server.network.ServerPlayerEntity
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

    private var _jobs = mutableMapOf<RegistryEntry<Job>, JobInstance>()
    val jobs: Map<RegistryEntry<Job>, JobInstance>
        get() = _jobs

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
        val newJobs = mutableMapOf<RegistryEntry<Job>, JobInstance>()
        for ((skill, maxLevel) in _skills)
            for (levelIndex in 0..<maxLevel) {
                val level = skill.value.levels[levelIndex]
                for (job in level.jobs) {
                    newJobs[job] = _jobs[job] ?: JobInstance(job.value)
                }
            }
        _jobs = newJobs
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: AbstractCriterion.Conditions> onCriterion(criterion: Criterion<T>, condition: Predicate<T>) {
        val player = player as? ServerPlayerEntity ?: return

        for ((jobEntry, instance) in _jobs) {
            val (tasks, cooldown) = instance
            if (cooldown > 0) continue
            
            val job = jobEntry.value
            for ((name, task) in job.tasks) {
                if (task.criteria.trigger != criterion || !condition.test(task.criteria.conditions as T)) continue

                val newCount = (tasks[name] ?: 0) + 1
                
                if (newCount >= task.count) {
                    // Complete task
                    tasks.remove(name)
                    if (tasks.isEmpty()) {
                        completeJob(player, job, instance)
                    }
                } else
                    tasks[name] = newCount
            }
        }
    }

    private fun completeJob(player: ServerPlayerEntity, job: Job, instance: JobInstance) {
        if (job.spawnAsOrbs) {
            SkillPointOrbEntity.spawnOrbs(player.serverWorld, player, player.pos, job.rewardPoints)
        } else {
            points += job.rewardPoints
            ServerPlayNetworking.send(player, SkillPointIncreasePayload)
        }
        instance.cooldown = job.cooldownTicks
        instance.resetTasks(job)
    }

    override fun serverTick() {
        for ((_, instance) in _jobs)
            if (instance.cooldown > 0)
                instance.cooldown--
    }

    override fun shouldCopyForRespawn(lossless: Boolean, keepInventory: Boolean, sameCharacter: Boolean): Boolean =
        sameCharacter

    override fun copyFrom(other: SkillsComponent, registryLookup: RegistryWrapper.WrapperLookup) {
        _skillClass = other._skillClass
        _skills = other._skills
        _points = other.points
        spentLevels = other.spentLevels
        _jobs = other._jobs
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

    data class JobInstance(
        val tasks: MutableMap<String, Int> = mutableMapOf(),
        var cooldown: Int = 0,
    ) {
        constructor(job: Job) : this() {
            resetTasks(job)
        }

        fun resetTasks(job: Job) {
            tasks.clear()
            for ((task, _) in job.tasks)
                tasks[task] = 0
        }

        companion object {
            val CODEC: Codec<JobInstance> = RecordCodecBuilder.create { it.group(
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("tasks").forGetter(JobInstance::tasks),
                Codec.INT.fieldOf("cooldown").forGetter(JobInstance::cooldown),
            ).apply(it) { tasks, cooldown -> JobInstance(tasks.toMutableMap(), cooldown) } }
        }
    }

    companion object : ComponentKeyHolder<SkillsComponent, PlayerEntity> {

        val CODEC = recordMutationCodec(
            RegistryFixedCodec.of(SkillClass.key).optionalFieldOf("class").forAccess(SkillsComponent::_skillClass),
            Codec.unboundedMap(RegistryFixedCodec.of(Skill.key), Codec.INT).mutate().fieldFor("skills", SkillsComponent::_skills),
            Codec.INT.fieldOf("spent").forAccess(SkillsComponent::spentLevels),
            Codec.INT.fieldOf("points").forAccess(SkillsComponent::_points),
            Codec.unboundedMap(RegistryFixedCodec.of(Job.key), JobInstance.CODEC).mutate().fieldFor("jobs", SkillsComponent::_jobs),
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

