package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Skill
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry

@JvmRecord
data class SkillUpgradePayload(val skill: RegistryKey<Skill>) : CustomPayload {
    constructor(skill: RegistryEntry<Skill>) : this(skill.key.get())

    override fun getId(): CustomPayload.Id<out SkillUpgradePayload> = ID

    companion object {
        val CODEC: PacketCodec<ByteBuf, SkillUpgradePayload> = PacketCodec.tuple(RegistryKey.createPacketCodec(Skill.key), SkillUpgradePayload::skill, ::SkillUpgradePayload)
        val ID: CustomPayload.Id<SkillUpgradePayload> = CustomPayload.Id(RPGSkills.id("skill_upgrade"))
    }
}