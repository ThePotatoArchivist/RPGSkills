package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.mixin.job.AdvancementCriterionAccessor
import archives.tater.rpgskills.util.RegistryKeyHolder
import archives.tater.rpgskills.util.intRangeCodec
import archives.tater.rpgskills.util.sequencedMapCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import java.util.SequencedMap
import java.util.SortedMap

@JvmRecord
data class Job(
    val name: String,
    val tasks: SequencedMap<String, Task>,
    val rewardPoints: Int,
    val cooldownTicks: Int,
    val spawnAsOrbs: Boolean = false,
) {
    constructor(
        name: String,
        tasks: Map<String, Task>,
        rewardPoints: Int,
        cooldownTicks: Int,
        spawnAsOrbs: Boolean = false,
    ) : this(name, LinkedHashMap(tasks), rewardPoints, cooldownTicks, spawnAsOrbs)

    @JvmRecord
    data class Task(
        val description: String,
        val count: Int,
        val criteria: AdvancementCriterion<*>,
    ) {
        companion object {
            val CODEC: MapCodec<Task> = RecordCodecBuilder.mapCodec { it.group(
                Codec.STRING.fieldOf("description").forGetter(Task::description),
                intRangeCodec(min = 1).fieldOf("count").forGetter(Task::count),
                AdvancementCriterionAccessor.getMAP_CODEC().forGetter(Task::criteria)
            ).apply(it, ::Task) }
        }
    }

    companion object : RegistryKeyHolder<Registry<Job>> {
        val CODEC: Codec<Job> = RecordCodecBuilder.create { it.group(
            Codec.STRING.fieldOf("name").forGetter(Job::name),
            sequencedMapCodec(Codec.STRING.fieldOf("id"), Task.CODEC).fieldOf("tasks").forGetter(Job::tasks),
            intRangeCodec(min = 0).fieldOf("reward_points").forGetter(Job::rewardPoints),
            intRangeCodec(min = 0).fieldOf("cooldown_ticks").forGetter(Job::cooldownTicks),
            Codec.BOOL.optionalFieldOf("spawn_as_orbs", false).forGetter(Job::spawnAsOrbs),
        ).apply(it, ::Job) }

        override val key: RegistryKey<Registry<Job>> = RegistryKey.ofRegistry(RPGSkills.id("job"))
    }
}
