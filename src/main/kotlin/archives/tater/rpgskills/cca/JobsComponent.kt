package archives.tater.rpgskills.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.cca.JobsComponent.JobInstance
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
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.Criterion
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.RegistryByteBuf
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

    private var _active = mutableListOf<JobInstance>()
    val active: List<JobInstance>
        get() = _active

    private var _cooldowns = mutableMapOf<RegistryEntry<Job>, Int>()
    val cooldowns: Map<RegistryEntry<Job>, Int>
        get() = _cooldowns

    var available = setOf<RegistryEntry<Job>>()
        private set

    val isFull get() = active.size >= MAX_JOBS

    // Runtime values, not saved or synced
    private var jobScreenOpen = false
    private var jobsUpdated = false

    operator fun get(job: RegistryEntry<Job>) = _active.find { it.job == job }

    operator fun contains(job: RegistryEntry<Job>) = _active.any { it.job == job }

    private fun sync() {
        key.sync(player)
        jobsUpdated = false
    }

    // Remove invalid jobs
    fun updateJobs(skills: Map<RegistryEntry<Skill>, Int>) {
        available = skills.entries.stream().flatMap { (skill, maxLevel) ->
            IntStream.range(0, maxLevel)
                .mapToObj { skill.value.levels[it] }
                .flatMap { it.jobs.stream() }
        }.collect(Collectors.toSet())

        if (player.world.isClient) return

        _active.removeIf { (job, _) -> job !in available }

        sync()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: AbstractCriterion.Conditions> onCriterion(criterion: Criterion<T>, conditionChecker: Predicate<T>) {
        val player = player as? ServerPlayerEntity ?: return

        val completed = mutableSetOf<JobInstance>()

        for (instance in _active) {
            val (jobEntry, tasks) = instance
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
            if (tasks.isEmpty()) {
                completeJob(player, instance)
                completed.add(instance)
            }
        }

        if (!completed.isEmpty()) {
            _active.removeAll(completed)
            sync()
        }
    }

    private fun completeJob(player: ServerPlayerEntity, instance: JobInstance) {
        val job = instance.job.value
        if (job.spawnAsOrbs) {
            SkillPointOrbEntity.spawnOrbs(player.serverWorld, player, player.pos, job.rewardPoints)
        } else with (player[SkillsComponent]) {
            if (!isPointsFull) {
                points += job.rewardPoints
                ServerPlayNetworking.send(player, SkillPointIncreasePayload)
            }
        }
        _cooldowns[instance.job] = job.cooldownTicks
        ServerPlayNetworking.send(player, JobCompletedPayload(instance.job))
    }

    fun tickCooldowns() {
        _cooldowns.removeIf { (job, cooldown) ->
            val newCooldown = cooldown - 1
            if (newCooldown <= 0) {
                true
            } else {
                _cooldowns[job] = newCooldown
                false
            }
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
            for (instance in active)
                instance.validate()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, tag, registryLookup).logIfError()
    }

    override fun applySyncPacket(buf: RegistryByteBuf?) {
        super.applySyncPacket(buf)
    }

    @JvmRecord
    data class JobInstance(
        val job: RegistryEntry<Job>,
        val tasks: MutableMap<String, Int> = job.value.tasks.mapValuesTo(mutableMapOf()) { 0 },
    ) {
        fun resetTasks() {
            tasks.clear()
            for ((task, _) in job.value.tasks)
                tasks[task] = 0
        }

        // Handle no-longer-existent keys
        fun validate() {
            tasks.keys
                .filter { it !in job.value.tasks }
                .forEach {
                    RPGSkills.logger.warn("Invalid task: {}/{}", job.idAsString, it)
                    tasks.remove(it)
                }
            if (tasks.isEmpty()) {
                RPGSkills.logger.warn("Job {} had no valid tasks, resetting", job.idAsString)
                resetTasks()
            }
        }

        companion object {
            val CODEC: Codec<JobInstance> = RecordCodecBuilder.create { it.group(
                RegistryFixedCodec.of(Job.key).fieldOf("job").forGetter(JobInstance::job),
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("tasks").forGetter(JobInstance::tasks)
            ).apply(it) { job, tasks -> JobInstance(job, tasks.toMutableMap()) } }
        }
    }

    companion object : ComponentKeyHolder<JobsComponent, PlayerEntity> {
        val CODEC = recordMutationCodec(
            JobInstance.CODEC.mutateCollection().fieldFor("active_jobs", JobsComponent::_active),
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
                if (!context.player()[SkillsComponent].isJobUnlocked(job)) {
                    RPGSkills.logger.warn("{} tried to add a job they didn't have: {}", context.player().gameProfile.name, job.key.orElseThrow().value)
                    return@registerGlobalReceiver
                }

                with(context.player()[JobsComponent]) {
                    when {
                        isFull -> RPGSkills.logger.warn(
                            "{} tried to add a job but was full: {}",
                            context.player().gameProfile.name,
                            job.key.orElseThrow().value
                        )
                        job in _cooldowns -> RPGSkills.logger.warn(
                            "{} tried to add a job that was on cooldown: {}",
                            context.player().gameProfile.name,
                            job.key.orElseThrow().value
                        )
                        _active.any { it.job == job } -> RPGSkills.logger.warn(
                            "{} tried to add a job they already have active: {}",
                            context.player().gameProfile.name,
                            job.key.orElseThrow().value
                        )
                        else -> {
                            _active.addFirst(JobInstance(job))
                            sync()
                        }
                    }
                }
            }

            ServerPlayNetworking.registerGlobalReceiver(RemoveJobPayload.ID) { (job), context ->
                with(context.player()[JobsComponent]) {
                    if (_active.removeIf { it.job == job })
                        sync()
                    else
                        RPGSkills.logger.warn("{} tried to remove a job they did not have: {}", context.player().gameProfile.name, job.key.orElseThrow().value)
                }
            }
        }
    }
}
