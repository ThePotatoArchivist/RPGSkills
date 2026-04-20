package archives.tater.rpgskills.item

import archives.tater.rpgskills.entity.SkillPointOrbEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class SkillNuggetItem(settings: Settings) : Item(settings) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        user.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK)

        if (world !is ServerWorld) return TypedActionResult.success(ItemStack.EMPTY)

        val stack = user.getStackInHand(hand)
        SkillPointOrbEntity.spawnOrbs(world, user, user.pos, stack.count)

        return TypedActionResult.success(ItemStack.EMPTY)
    }
}