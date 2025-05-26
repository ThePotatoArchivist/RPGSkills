package archives.tater.rpgskills.entity

import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.util.get
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.MovementType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.registry.tag.FluidTags
import net.minecraft.server.network.EntityTrackerEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper.square
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.sqrt

class SkillPointOrbEntity(type: EntityType<out SkillPointOrbEntity>, world: World) : Entity(type, world) {
    private var orbAge = 0
    private var health = 5

    private var ownerUuid: UUID? = null
    private var _owner: PlayerEntity? = null
    var owner: PlayerEntity?
        get() {
            if (_owner?.let { !it.isRemoved } == true) return _owner
            if (ownerUuid == null) return null
            _owner = (world as? ServerWorld ?: return null).getEntity(ownerUuid) as? PlayerEntity
            return _owner
        }
        set(value) {
            ownerUuid = value?.uuid
            _owner = value
        }
    private var nearOwner = false

    constructor(world: World, x: Double, y: Double, z: Double, owner: PlayerEntity?) : this(RPGSkillsEntities.SKILL_POINT_ORB, world) {
        setPosition(x, y, z)
        this.owner = owner
        yaw = random.nextFloat() * 360f
        setVelocity(
            random.nextDouble() * 0.4 - 0.2,
            random.nextDouble() * 0.4,
            random.nextDouble() * 0.4 - 0.2,
        )
    }

    override fun getMoveEffect(): MoveEffect = MoveEffect.NONE

    override fun initDataTracker(builder: DataTracker.Builder?) {
    }

    override fun getGravity(): Double = 0.03

    override fun tick() {
        super.tick()
        prevX = x
        prevY = y
        prevZ = z
        if (isSubmergedIn(FluidTags.WATER))
            applyWaterMovement()
        else
            applyGravity()

        if (world.getFluidState(blockPos).isIn(FluidTags.LAVA))
            setVelocity(
                0.2 * random.nextDouble() - 0.1,
                0.2,
                0.2 * random.nextDouble() - 0.1,
            )

        if (!world.isSpaceEmpty(boundingBox))
            pushOutOfBlocks(x, (boundingBox.minY + boundingBox.maxY) / 2.0, z)

        if (age % EXPENSIVE_UPDATE_INTERVAL == 1)
            nearOwner = owner?.let { it.squaredDistanceTo(this) <= 64.0 } ?: false

        if (owner?.let { it.isSpectator || it.isDead } == true)
            nearOwner = false

        if (nearOwner) owner?.let { owner ->
            val targetDelta = Vec3d(owner.x - x, owner.y + owner.standingEyeHeight / 2.0 - y, owner.z - z)
            val lengthSquared = targetDelta.lengthSquared()
            if (lengthSquared < 64.0) {
                velocity = velocity.add(targetDelta.normalize().multiply(square(1.0 - sqrt(lengthSquared) / MAX_TARGET_DISTANCE) * 0.1))
            }
        }

        move(MovementType.SELF, velocity)

        val friction = if (isOnGround) world.getBlockState(velocityAffectingPos).block.slipperiness * 0.98 else 0.98
        velocity = velocity.multiply(friction, 0.98, friction)
        if (isOnGround)
            velocity = velocity.multiply(1.0, -0.9, 1.0)

        ++orbAge
        if (orbAge >= DESPAWN_AGE)
            discard()
    }

    override fun getVelocityAffectingPos(): BlockPos = getPosWithYOffset(0.999999f)

    private fun applyWaterMovement() {
        val velocity = velocity
        setVelocity(velocity.x * 0.99F, (velocity.y + 5.0E-4F).coerceAtMost(0.06), velocity.z * 0.99F)
    }

    override fun onSwimmingStart() {
    }

    override fun damage(source: DamageSource, amount: Float): Boolean {
        if (isInvulnerableTo(source)) return false
        if (world.isClient) return true

        scheduleVelocityUpdate()
        health = (health - amount).toInt()
        if (health <= 0)
            discard()

        return true
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putShort("Health", health.toShort())
        nbt.putShort("Age", orbAge.toShort())
        ownerUuid ?.let { nbt.putUuid("Owner", it) }
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        health = nbt.getShort("Health").toInt()
        orbAge = nbt.getShort("Age").toInt()
        if (nbt.containsUuid("Owner")) {
            _owner = null
            ownerUuid = nbt.getUuid("Owner")
        }
    }

    override fun createSpawnPacket(entityTrackerEntry: EntityTrackerEntry): Packet<ClientPlayPacketListener> =
        EntitySpawnS2CPacket(this, entityTrackerEntry, owner?.id ?: 0)

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)
        owner = world.getEntityById(packet.entityData) as? PlayerEntity
    }

    override fun isAttackable(): Boolean = false

    override fun getSoundCategory(): SoundCategory = SoundCategory.AMBIENT

    override fun onPlayerCollision(player: PlayerEntity?) {
        if (player !is ServerPlayerEntity || (owner != null && player != owner) || player.experiencePickUpDelay > 0) return
        player.experiencePickUpDelay = 2
        player.sendPickup(this, 1)
        player[SkillsComponent].addSkillPoints(1)
        discard()
    }

    companion object {
        private const val DESPAWN_AGE = 6000
        private const val EXPENSIVE_UPDATE_INTERVAL = 20
        private const val MAX_TARGET_DISTANCE = 8

        @JvmStatic
        fun spawnOrbs(world: ServerWorld, owner: PlayerEntity?, pos: Vec3d, amount: Int) {
            repeat (amount) {
                world.spawnEntity(SkillPointOrbEntity(world, pos.getX(), pos.getY(), pos.getZ(), owner))
            }
        }

    }
}
