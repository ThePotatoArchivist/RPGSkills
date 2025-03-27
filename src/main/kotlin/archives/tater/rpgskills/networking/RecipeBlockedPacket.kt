package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.LockGroup
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.RegistryKey

data class RecipeBlockedPacket(val lockGroup: RegistryKey<LockGroup>?) : FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeBoolean(lockGroup != null)
        lockGroup?.let(buf::writeRegistryKey)
    }

    override fun getType(): PacketType<RecipeBlockedPacket> = TYPE

    companion object {
        val TYPE: PacketType<RecipeBlockedPacket> = PacketType.create(RPGSkills.id("recipe_blocked"), ::read)

        @JvmField
        val EMPTY = RecipeBlockedPacket(null)

        fun read(buf: PacketByteBuf) =
            if (buf.readBoolean())RecipeBlockedPacket(buf.readRegistryKey(LockGroup.key)) else EMPTY
    }
}