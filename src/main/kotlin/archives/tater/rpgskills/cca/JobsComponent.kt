package archives.tater.rpgskills.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.entity.SkillPointOrbEntity
import archives.tater.rpgskills.networking.AddJobPayload
import archives.tater.rpgskills.networking.CloseJobScreenPayload
import archives.tater.rpgskills.networking.JobCompletedPayload
import archives.tater.rpgskills.networking.OpenJobScreenPayload
import archives.tater.rpgskills.networking.RemoveJobPayload
import archives.tater.rpgskills.networking.SkillPointIncreasePayload
import archives.tater.rpgskills.util.*
import archives.tater.rpgskills.util.get
import com.mojang.serialization.Codec
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.predicate.entity.EntityPredicate.createAdvancementEntityLootContext
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryFixedCodec
import net.minecraft.server.network.ServerPlayerEntity
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent
import org.ladysnake.cca.api.v3.entity.RespawnableComponent
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.collections.iterator
import kotlin.jvm.optionals.getOrNull

@Suppress("UnstableApiUsage")
class JobsComponent(private val player: PlayerEntity) : RespawnableComponent<JobsComponent>, AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {

    private var _active = mutableMapOf<RegistryEntry<Job>, JobInstance>()
    val active: Map<RegistryEntry<Job>, JobInstance>
        get() = _active

    private var _cooldowns = mutableMapOf<RegistryEntry<Job>, Int>()
    val cooldowns: Map<RegistryEntry<Job>, Int>
        get() = _cooldowns

    // Runtime values, not saved or synced
    private var jobScreenOpen = false
    private var jobsUpdated = false

    private fun sync() {
        key.sync(player)
        jobsUpdated = false
    }

    // Remove invalid jobs
    fun updateJobs(skills: Map<RegistryEntry<Skill>, Int>) {
        if (player.world.isClient) return

        val validJobs = skills.entries.stream().flatMap { (skill, maxLevel) ->
            IntStream.range(0, maxLevel)
                .mapToObj { skill.value.levels[it] }
                .flatMap { it.jobs.stream() }
        }.collect(Collectors.toSet())

        _active.removeIf { (job, _) -> job !in validJobs }

        sync()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: AbstractCriterion.Conditions> onCriterion(criterion: Criterion<T>, conditionChecker: Predicate<T>) {
        val player = player as? ServerPlayerEntity ?: return

        for ((jobEntry, instance) in _active) {
            val (tasks) = instance

            val job = jobEntry.value
            for ((name, task) in job.tasks) {
                if (name !in tasks) continue
                if (task.criteria.trigger != criterion) continue
                val conditions = task.criteria.conditions as T
                if (conditions.player().getOrNull()?.test(createAdvancementEntityLootContext(player, player)) == false) continue
                if (!conditionChecker.test(conditions)) continue

                val newCount = (tasks[name] ?: 0) + 1

                if (newCount >= task.count) {
                    tasks.remove(name)
                } else
                    tasks[name] = newCount

                jobsUpdated = true
            }
            if (tasks.isEmpty())
                completeJob(player, jobEntry, instance)
        }
    }

    private fun completeJob(player: ServerPlayerEntity, jobEntry: RegistryEntry<Job>, instance: JobInstance) {
        val job = jobEntry.value
        if (job.spawnAsOrbs) {
            SkillPointOrbEntity.spawnOrbs(player.serverWorld, player, player.pos, job.rewardPoints)
        } else with (player[SkillsComponent]) {
            if (!isPointsFull) {
                points += job.rewardPoints
                ServerPlayNetworking.send(player, SkillPointIncreasePayload)
            }
        }
        _cooldowns[jobEntry] = job.cooldownTicks
        instance.resetTasks(job)
        ServerPlayNetworking.send(player, JobCompletedPayload(jobEntry))
    }

    fun tickCooldowns() {
        for ((job, cooldown) in _cooldowns) {
            val newCooldown = cooldown - 1
            if (newCooldown <= 0)
                _cooldowns.remove(job)
            else
                _cooldowns[job] = newCooldown
        }
    }

    override fun serverTick() {
        val syncCooldowns = _cooldowns.any { (_, cooldown) -> cooldown > 0 } && player.age % JOB_SYNC_FREQUENCY == 0
        tickCooldowns()
        if (syncCooldowns || jobScreenOpen && jobsUpdated && player.age % JOB_SCREEN_SYNC_FREQUENCY == 0)
            sync()
    }

    override fun clientTick() {
        tickCooldowns()
    }

    override fun copyFrom(other: JobsComponent, registryLookup: RegistryWrapper.WrapperLookup) {
        _active = other._active
        _cooldowns = other._cooldowns
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.update(this, tag, registryLookup).logIfError()
        if (!player.world.isClient)
            for ((job, instance) in active)
                instance.validate(job)
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, tag, registryLookup).logIfError()
    }

    @JvmRecord
    data class JobInstance(
        val tasks: MutableMap<String, Int> = mutableMapOf(),
    ) {
        constructor(job: Job) : this() {
            resetTasks(job)
        }

        fun resetTasks(job: Job) {
            tasks.clear()
            for ((task, _) in job.tasks)
                tasks[task] = 0
        }

        // Handle no-longer-existent keys
        fun validate(job: RegistryEntry<Job>) {
            tasks.keys
                .filter { it !in job.value.tasks }
                .forEach {
                    RPGSkills.logger.warn("Invalid task: {}/{}", job.idAsString, it)
                    tasks.remove(it)
                }
            if (tasks.isEmpty()) {
                RPGSkills.logger.warn("Job {} had no valid tasks, resetting", job.idAsString)
                resetTasks(job.value)
            }
        }

        companion object {
            val CODEC: Codec<JobInstance> =
                Codec.unboundedMap(Codec.STRING, Codec.INT).xmap({ JobInstance(it.toMutableMap()) }, JobInstance::tasks)
        }
    }

    companion object : ComponentKeyHolder<JobsComponent, PlayerEntity> {
        val CODEC = recordMutationCodec(
            Codec.unboundedMap(RegistryFixedCodec.of(Job.key), JobInstance.CODEC).mutate().fieldFor("active_jobs", JobsComponent::_active),
            Codec.unboundedMap(RegistryFixedCodec.of(Job.key), intRangeCodec(min = 1)).mutate().fieldFor("cooldowns", JobsComponent::_cooldowns),
        )

        @JvmField
        val KEY: ComponentKey<JobsComponent> = ComponentRegistry.getOrCreate(RPGSkills.id("jobs"), JobsComponent::class.java)

        override val key get() = KEY

        const val JOB_SYNC_FREQUENCY = 20 * 60 // 1 minute
        const val JOB_SCREEN_SYNC_FREQUENCY = 20 * 2 // 2 seconds

        const val MAX_JOBS = 10

        fun registerEvents() {
            ServerPlayNetworking.registerGlobalReceiver(OpenJobScreenPayload.id) { _, context ->
                with(context.player()[JobsComponent]) {
                    jobScreenOpen = true
                    sync()
                }
            }

            ServerPlayNetworking.registerGlobalReceiver(CloseJobScreenPayload.id) { _, context ->
                with(context.player()[JobsComponent]) {
                    jobScreenOpen = false
                }
            }

            ServerPlayNetworking.registerGlobalReceiver(AddJobPayload.ID) { (job), context ->
                if (!context.player()[SkillsComponent].isJobUnlocked(job)) return@registerGlobalReceiver

                with(context.player()[JobsComponent]) {
                    _active[job] = JobInstance(job.value)
                }
            }

            ServerPlayNetworking.registerGlobalReceiver(RemoveJobPayload.ID) { (job), context ->
                with(context.player()[JobsComponent]) {
                    _active.remove(job)
                }
            }
        }
    }
}
