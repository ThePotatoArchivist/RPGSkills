package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Job
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier

class SingleFieldPayload<T>(id: Identifier, codec: PacketCodec<in RegistryByteBuf, T>): (T) -> SingleFieldPayload.Instance<T> {
    val id = CustomPayload.Id<Instance<T>>(id)
    val codec: PacketCodec<in RegistryByteBuf, Instance<T>> = codec.xmap(::invoke, Instance<T>::value)

    override fun invoke(p1: T): Instance<T> = Instance(id, p1)

    @JvmRecord
    data class Instance<T>(private val id: CustomPayload.Id<Instance<T>>, val value: T) : CustomPayload {
        override fun getId(): CustomPayload.Id<out Instance<T>> = id
    }
}

fun <T> PayloadTypeRegistry<out RegistryByteBuf>.register(payload: SingleFieldPayload<T>) {
    register(payload.id, payload.codec)
}

val REMOVE_JOB = SingleFieldPayload<RegistryEntry<Job>>(RPGSkills.id("remove_job"), PacketCodecs.registryEntry(Job.key))

fun test() {
    REMOVE_JOB(RegistryEntry.of(Job("a", mapOf(), 1, 1)))
}
