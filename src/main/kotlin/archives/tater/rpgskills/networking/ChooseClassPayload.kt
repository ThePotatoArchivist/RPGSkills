package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id

object ChooseClassPayload : CustomPayload {
    override fun getId(): Id<out CustomPayload> = ID

    val CODEC: PacketCodec<ByteBuf, ChooseClassPayload> = PacketCodec.unit(this)
    val ID = Id<ChooseClassPayload>(RPGSkills.id("choose_class"))
}