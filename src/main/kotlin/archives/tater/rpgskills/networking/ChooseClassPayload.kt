package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id

object ChooseClassPayload : SingletonPayload<ChooseClassPayload>(RPGSkills.id("choose_class"))