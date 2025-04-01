package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Skill
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.entry.RegistryEntry

@JvmRecord
data class SkillUpgradePayload(val skill: RegistryEntry<Skill>) : CustomPayload {

    override fun getId(): CustomPayload.Id<out SkillUpgradePayload> = ID

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, SkillUpgradePayload> = PacketCodec.tuple(PacketCodecs.registryEntry(Skill.key), SkillUpgradePayload::skill, ::SkillUpgradePayload)
        val ID: CustomPayload.Id<SkillUpgradePayload> = CustomPayload.Id(RPGSkills.id("skill_upgrade"))
    }
}