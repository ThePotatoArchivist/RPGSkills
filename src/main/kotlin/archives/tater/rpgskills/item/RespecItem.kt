package archives.tater.rpgskills.item

import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.get
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class RespecItem(settings: Settings) : Item(settings) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (world is ServerWorld) {
            user[SkillsComponent].resetSkillsToClass()
            world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, user.x, user.getBodyY(0.5), user.z, 16, user.width.toDouble() / 2, user.height.toDouble() / 2, user.width.toDouble() / 2, 0.0)
        }
        user.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP)
        stack.decrement(1)
        return TypedActionResult.success(stack)
    }
}
