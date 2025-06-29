package archives.tater.rpgskills

import archives.tater.rpgskills.data.LockGroup
import io.wispforest.accessories.api.events.CanEquipCallback
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.util.TriState
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.TypedActionResult

private inline fun failIfLocked(player: PlayerEntity, getMessage: (LockGroup) -> Text, findGroup: () -> LockGroup?): ActionResult {
    if (player.isSpectator)
        return ActionResult.PASS
    val lockGroup = findGroup() ?: return ActionResult.PASS
    player.sendMessage(getMessage(lockGroup), true)
    return ActionResult.FAIL
}

internal fun registerLockEvents() {

    UseItemCallback.EVENT.register { player, _, hand ->
        val stack = player.getStackInHand(hand)
        TypedActionResult(failIfLocked(player, LockGroup::itemMessage) { LockGroup.findLocked(player, stack) }, stack)
    }

    UseEntityCallback.EVENT.register { player, _, _, entity, _ ->
        failIfLocked(player, LockGroup::entityMessage) { LockGroup.findLocked(player, entity) }
    }

    UseBlockCallback.EVENT.register { player, world, hand, hit ->
        failIfLocked(player, LockGroup::blockMessage) {
            LockGroup.findLocked(player, world.getBlockState(hit.blockPos))
        }.takeUnless { it == ActionResult.PASS }
        ?: failIfLocked(player, LockGroup::itemMessage) {
            LockGroup.findLocked(player, player.getStackInHand(hand))
        }
    }

    AttackBlockCallback.EVENT.register { player, _, hand, _, _ ->
        failIfLocked(player, LockGroup::itemMessage) {
            LockGroup.findLocked(player, player.getStackInHand(hand))
        }
    }

    if (FabricLoader.getInstance().isModLoaded("accessories")) {
        CanEquipCallback.EVENT.register { stack, slotReference ->
            if ((slotReference.entity() as? PlayerEntity)?.let { LockGroup.isLocked(it, stack) } == true)
                TriState.FALSE
            else
                TriState.DEFAULT
        }
    }
}

fun checkAttackLocked(player: PlayerEntity) = failIfLocked(player, LockGroup::itemMessage) {
    LockGroup.findLocked(player, player.mainHandStack)
} != ActionResult.PASS
