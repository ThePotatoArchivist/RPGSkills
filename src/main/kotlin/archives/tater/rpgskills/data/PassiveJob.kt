package archives.tater.rpgskills.data

import archives.tater.rpgskills.util.intRangeCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.AdvancementCriterion

data class PassiveJob(
    val criteria: AdvancementCriterion<*>,
    val rewardPoints: Int,
    val cooldownTicks: Int,
    val spawnAsOrbs: Boolean = false,
) {
    companion object {
        val CODEC: Codec<PassiveJob> = RecordCodecBuilder.create { it.group(
            AdvancementCriterion.CODEC.fieldOf("criteria").forGetter(PassiveJob::criteria),
            intRangeCodec(min = 0).fieldOf("reward_points").forGetter(PassiveJob::rewardPoints),
            intRangeCodec(min = 0).fieldOf("cooldown_ticks").forGetter(PassiveJob::cooldownTicks),
            Codec.BOOL.optionalFieldOf("spawn_as_orbs", false).forGetter(PassiveJob::spawnAsOrbs),
        ).apply(it, ::PassiveJob) }
    }
}