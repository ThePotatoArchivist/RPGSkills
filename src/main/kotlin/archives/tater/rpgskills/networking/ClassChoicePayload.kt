package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.SkillClass
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.registry.entry.RegistryEntry

class ClassChoicePayload(val skillClass: RegistryEntry<SkillClass>) : CustomPayload {
    override fun getId(): Id<out CustomPayload> = ID

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, ClassChoicePayload> =
            PacketCodec.tuple(PacketCodecs.registryEntry(SkillClass.key), ClassChoicePayload::skillClass, ::ClassChoicePayload)

        val ID = Id<ClassChoicePayload>(RPGSkills.id("class_choice"))
    }
}