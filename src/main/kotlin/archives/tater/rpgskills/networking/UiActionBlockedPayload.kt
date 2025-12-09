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
data class UiActionBlockedPayload(val lockGroup: Optional<LockGroup>) : CustomPayload {
    constructor(lockGroup: LockGroup?) : this(Optional.ofNullable(lockGroup))

    override fun getId(): Id<out UiActionBlockedPayload> = ID

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, UiActionBlockedPayload> = PacketCodec.tuple(PacketCodecs.optional(PacketCodecs.registryValue(LockGroup.key)), UiActionBlockedPayload::lockGroup) {
            if (it.isEmpty) EMPTY else UiActionBlockedPayload(it)
        }
        val ID = Id<UiActionBlockedPayload>(RPGSkills.id("ui_action_blocked"))

        @JvmField
        val EMPTY = UiActionBlockedPayload(Optional.empty())
    }
}