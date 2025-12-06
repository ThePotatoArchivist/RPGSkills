package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Job
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.entry.RegistryEntry

@JvmRecord
data class RemoveJobPayload(val job: RegistryEntry<Job>) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload> = ID

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, RemoveJobPayload> = PacketCodecs.registryEntry(Job.key).xmap(::RemoveJobPayload, RemoveJobPayload::job)
        val ID = CustomPayload.Id<RemoveJobPayload>(RPGSkills.id("remove_job"))
    }
}