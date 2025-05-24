package archives.tater.rpgskills.entity

import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.mixin.xp.ExperienceOrbAccessor
import archives.tater.rpgskills.util.get
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.ExperienceOrbEntity.roundToOrbSize
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class SkillPointOrbEntity(type: EntityType<out SkillPointOrbEntity>, world: World) : Entity(type, world) {

    @Suppress("CAST_NEVER_SUCCEEDS")
    private inline val thisAccess get() = this as ExperienceOrbAccessor

    constructor(world: World, x: Double, y: Double, z: Double, amount: Int) : this(RPGSkillsEntities.SKILL_POINT_ORB, world) {
        setPosition(x, y, z)
        yaw = (random.nextDouble() * 360.0).toFloat()
        setVelocity((random.nextDouble() * 0.2f - 0.1f) * 2.0, random.nextDouble() * 0.2 * 2.0, (random.nextDouble() * 0.2f - 0.1f) * 2.0)
        thisAccess.setAmount(amount)
    }

    override fun initDataTracker(builder: DataTracker.Builder?) {
        TODO("Not yet implemented")
    }

    override fun onPlayerCollision(player: PlayerEntity?) {
        if (player !is ServerPlayerEntity) return
        player.experiencePickUpDelay = 2
        player.sendPickup(this, 1)
        player[SkillsComponent].addSkillPoints(0 /*experienceAmount*/)
        this.discard()
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound?) {
        TODO("Not yet implemented")
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound?) {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmStatic
        fun spawnOrbs(world: ServerWorld, pos: Vec3d, amount: Int) {
            var remaining = amount
            while (remaining > 0) {
                val size = roundToOrbSize(remaining)
                remaining -= size
                world.spawnEntity(SkillPointOrbEntity(world, pos.getX(), pos.getY(), pos.getZ(), size))
            }
        }

    }
}
