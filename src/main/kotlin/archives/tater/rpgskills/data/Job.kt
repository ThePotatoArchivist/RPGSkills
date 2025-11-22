package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Job.Task
import archives.tater.rpgskills.util.RegistryKeyHolder
import archives.tater.rpgskills.util.intRangeCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import com.ibm.icu.lang.UCharacter.GraphemeClusterBreak.T

data class Job(
    val tasks: Map<String, Task>,
    val rewardPoints: Int,
    val cooldownTicks: Int,
    val spawnAsOrbs: Boolean = false,
) {
    data class Task(
        val criteria: AdvancementCriterion<*>,
        val count: Int,
    ) {
        companion object {
            val CODEC: Codec<Task> = RecordCodecBuilder.create { it.group(
                AdvancementCriterion.CODEC.fieldOf("criteria").forGetter(Task::criteria),
                intRangeCodec(min = 1).fieldOf("count").forGetter(Task::count),
            ).apply(it, ::Task) }
        }
    }

    companion object : RegistryKeyHolder<Registry<Job>> {
        val CODEC: Codec<Job> = RecordCodecBuilder.create { it.group(
            Codec.unboundedMap(Codec.STRING, Task.CODEC).fieldOf("tasks").forGetter(Job::tasks),
            intRangeCodec(min = 0).fieldOf("reward_points").forGetter(Job::rewardPoints),
            intRangeCodec(min = 0).fieldOf("cooldown_ticks").forGetter(Job::cooldownTicks),
            Codec.BOOL.optionalFieldOf("spawn_as_orbs", false).forGetter(Job::spawnAsOrbs),
        ).apply(it, ::Job) }

        override val key: RegistryKey<Registry<Job>> = RegistryKey.ofRegistry(RPGSkills.id("job"))
    }
}