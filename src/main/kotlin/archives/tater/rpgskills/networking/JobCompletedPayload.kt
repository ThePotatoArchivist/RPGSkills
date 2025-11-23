package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Job
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryFixedCodec

class JobCompletedPayload(val job: RegistryEntry<Job>) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload> = ID

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, JobCompletedPayload> = PacketCodecs.registryEntry(Job.key).xmap(::JobCompletedPayload, JobCompletedPayload::job)
        val ID = CustomPayload.Id<JobCompletedPayload>(RPGSkills.id("job_completed"))
    }
}