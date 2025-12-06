package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import io.netty.buffer.ByteBuf
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

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

data object ChooseClassPayload : SingletonPayload<ChooseClassPayload>(RPGSkills.id("choose_class"))
data object SkillPointIncreasePayload : SingletonPayload<SkillPointIncreasePayload>(RPGSkills.id("skill_point_increase"))
data object OpenJobScreenPayload : SingletonPayload<OpenJobScreenPayload>(RPGSkills.id("open_job_screen"))
data object CloseJobScreenPayload : SingletonPayload<CloseJobScreenPayload>(RPGSkills.id("close_job_screen"))
