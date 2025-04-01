package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.LockGroup
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.registry.RegistryKey

@JvmRecord
data class RecipeBlockedPayload(val lockGroup: RegistryKey<LockGroup>?) : CustomPayload {

    override fun getId(): Id<out RecipeBlockedPayload> = ID

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, RecipeBlockedPayload> = PacketCodec.tuple(RegistryKey.createPacketCodec(LockGroup.key), RecipeBlockedPayload::lockGroup) {
            if (it == null) EMPTY else RecipeBlockedPayload(it)
        }
        val ID: Id<RecipeBlockedPayload> = Id(RPGSkills.id("recipe_blocked"))

        @JvmField
        val EMPTY = RecipeBlockedPayload(null)
    }
}