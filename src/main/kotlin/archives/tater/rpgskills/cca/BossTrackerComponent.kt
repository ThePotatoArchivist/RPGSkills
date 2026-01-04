package archives.tater.rpgskills.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.util.*
import archives.tater.rpgskills.util.get
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.Monster
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Formatting
import net.minecraft.world.World
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import kotlin.collections.iterator
import kotlin.jvm.optionals.getOrNull

class BossTrackerComponent(private val world: World) : Component, AutoSyncedComponent {

    private val _defeated = mutableSetOf<EntityType<*>>()
    val defeated: Set<EntityType<*>> get() = _defeated
    val defeatedCount get() = _defeated.size

    var maxLevel: Int = if (world.isClient) Int.MAX_VALUE else RPGSkills.CONFIG.baseLevelCap
        private set

    val increasesLevelCap by lazy {
        world.registryManager[RegistryKeys.ENTITY_TYPE]
            .getEntryList(RPGSkillsTags.INCREASES_LEVEL_CAP)
            .getOrNull()
            ?: RegistryEntryList.empty()
    }
    val totalCount get() = increasesLevelCap.size()

    fun hasDefeated(entity: LivingEntity) = entity.type in defeated

    fun onDefeated(entity: LivingEntity): Boolean {
        if (Registries.ENTITY_TYPE.getEntry(entity.type) !in increasesLevelCap) return false
        if (hasDefeated(entity)) return false
        _defeated.add(entity.type)
        updateLevelCap()
        key.sync(world)

        if (entity is MobEntity)
            for (uuid in entity[DefeatSourceComponent].attackers.keys)
                world.server?.playerManager?.getPlayer(uuid)?.networkHandler
                    ?.sendPacket(TitleS2CPacket(BOSS_DEFEAT_TITLE.text))

        world.server?.playerManager?.apply {
            broadcast(BOSS_DEFEAT_MESSAGE.text(entity.type.name), false)
            broadcast(ENEMIES_STRENGTHEN_MESSAGE.text, false)
            broadcast(if (maxLevel < Int.MAX_VALUE) CAP_RAISE_MESSAGE.text(maxLevel) else CAP_REMOVED_MESSAGE.text, false)
        }
        return true
    }

    private fun updateLevelCap() {
        maxLevel = if (defeatedCount >= increasesLevelCap.size())
            Int.MAX_VALUE
        else
            RPGSkills.CONFIG.baseLevelCap + RPGSkills.CONFIG.levelCapIncreasePerBoss * defeatedCount
    }

    fun reset() {
        _defeated.clear()
        updateLevelCap()
        key.sync(world)
    }

    fun add(entity: EntityType<*>): Boolean {
        if (!(entity isIn RPGSkillsTags.INCREASES_LEVEL_CAP)) return false
        if (!_defeated.add(entity)) return false
        updateLevelCap()
        key.sync(world)
        return true
    }

    fun remove(entity: EntityType<*>): Boolean {
        if (!_defeated.remove(entity)) return false
        updateLevelCap()
        key.sync(world)
        return true
    }

    override fun readFromNbt(
        tag: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup
    ) {
        CODEC.update(this, tag).logIfError()
        if (!world.isClient)
            updateLevelCap()
    }

    override fun writeToNbt(
        tag: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup
    ) {
        CODEC.encode(this, tag).logIfError()
    }

    private fun copy(other: BossTrackerComponent) {
        _defeated.clear()
        _defeated.addAll(other._defeated)
        updateLevelCap()
    }

    companion object : ComponentKeyHolder<BossTrackerComponent, World> {
        override val key: ComponentKey<BossTrackerComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("level_cap"), BossTrackerComponent::class.java)

        val CODEC = recordMutationCodec(
            Registries.ENTITY_TYPE.codec.mutateCollection().fieldFor("defeated_bosses", BossTrackerComponent::_defeated),
            intRangeCodec(min = 0).fieldOf("max_level").forAccess(BossTrackerComponent::maxLevel),
        )

        val BOSS_DEFEAT_TITLE = Translation.unit("rpgskills.title.boss_defeat") {
            formatted(Formatting.AQUA)
        }

        val BOSS_DEFEAT_MESSAGE = Translation.arg("rpgskills.announcement.boss_defeat") {
            formatted(Formatting.AQUA)
        }

        val ENEMIES_STRENGTHEN_MESSAGE = Translation.unit("rpgskills.announcement.enemies_strengthen") {
            formatted(Formatting.AQUA)
        }

        val CAP_RAISE_MESSAGE = Translation.arg("rpgskills.announcement.levelcap.raised") {
            formatted(Formatting.AQUA)
        }
        val CAP_REMOVED_MESSAGE = Translation.unit("rpgskills.announcement.levelcap.removed") {
            formatted(Formatting.AQUA)
        }

        val BOSS_DEFEAT_SCALING = RPGSkills.id("bosses_defeated_bonus")

        fun update(server: MinecraftServer, action: BossTrackerComponent.() -> Boolean) {
            val component = server.overworld[BossTrackerComponent]
            if (!component.action()) return
            for (world in server.worlds) {
                if (world == server.overworld) continue
                world[BossTrackerComponent].copy(component)
                key.sync(world)
            }
        }

        @JvmStatic
        fun onDeath(entity: LivingEntity) {
            update(entity.world.server!!) {
                onDefeated(entity)
            }
        }

        @JvmStatic
        fun applyBuffs(entity: LivingEntity) {
            if (entity !is Monster) return

            val defeated = entity.world[BossTrackerComponent].defeatedCount
            if (defeated < 0) return

            if (entity isIn RPGSkillsTags.BOSS_ATTRIBUTE_AFFECTED)
                for ((attribute, modifier) in RPGSkills.CONFIG.bossAttributeIncreases[defeated.coerceAtMost(RPGSkills.CONFIG.bossAttributeIncreases.size - 1)])
                    entity.getAttributeInstance(attribute)?.addPersistentModifier(modifier.build(BOSS_DEFEAT_SCALING, 1.0))
            else
                for ((attribute, modifier) in RPGSkills.CONFIG.attributeIncreases)
                    entity.getAttributeInstance(attribute)?.addPersistentModifier(modifier.build(BOSS_DEFEAT_SCALING, defeated.toDouble()))

            if (entity.health < entity.maxHealth)
                entity.health = entity.maxHealth
        }
    }
}
