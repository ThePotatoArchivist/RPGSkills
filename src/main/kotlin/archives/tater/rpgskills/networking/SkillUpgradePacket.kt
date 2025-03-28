package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Skill
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry

@JvmRecord
data class SkillUpgradePacket(val skill: RegistryKey<Skill>) : FabricPacket {
    constructor(skill: RegistryEntry<Skill>) : this(skill.key.get())

    override fun write(buf: PacketByteBuf) {
        buf.writeRegistryKey(skill)
    }

    override fun getType(): PacketType<SkillUpgradePacket> = TYPE

    companion object {
        val TYPE: PacketType<SkillUpgradePacket> = PacketType.create(RPGSkills.id("skill_upgrade"), ::read)

        fun read(buf: PacketByteBuf) = SkillUpgradePacket(buf.readRegistryKey(Skill.key))
    }
}