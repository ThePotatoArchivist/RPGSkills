package archives.tater.rpgskills.networking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.LockGroup
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import java.util.*

@JvmRecord
data class RecipeBlockedPayload(val lockGroup: Optional<LockGroup>) : CustomPayload {
    constructor(lockGroup: LockGroup?) : this(Optional.ofNullable(lockGroup))

    override fun getId(): Id<out RecipeBlockedPayload> = ID

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, RecipeBlockedPayload> = PacketCodec.tuple(PacketCodecs.optional(PacketCodecs.registryValue(LockGroup.key)), RecipeBlockedPayload::lockGroup) {
            if (it.isEmpty) EMPTY else RecipeBlockedPayload(it)
        }
        val ID = Id<RecipeBlockedPayload>(RPGSkills.id("recipe_blocked"))

        @JvmField
        val EMPTY = RecipeBlockedPayload(Optional.empty())
    }
}