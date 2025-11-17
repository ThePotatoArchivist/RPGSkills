package archives.tater.rpgskills.networking

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import com.mojang.serialization.Codec
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import io.netty.buffer.ByteBuf

abstract class SingletonPayload<T: SingletonPayload<T>>(id: Identifier) : CustomPayload {
    @JvmField
    val id = CustomPayload.Id<T>(id)
    @JvmField
    @Suppress("UNCHECKED_CAST")
    val codec: PacketCodec<ByteBuf, T> = PacketCodec.unit<ByteBuf, T>(this as T)

    override fun getId(): CustomPayload.Id<T> = id
}

fun <T: SingletonPayload<T>> PayloadTypeRegistry<out PacketByteBuf>.register(payload: T) {
    register(payload.id, payload.codec)
}