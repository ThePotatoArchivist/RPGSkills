package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.util.*
import archives.tater.rpgskills.util.get
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.Monster
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.util.Formatting
import net.minecraft.world.World
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import kotlin.collections.iterator
import kotlin.jvm.optionals.getOrNull

class BossTrackerComponent(private val world: World) : Component, AutoSyncedComponent {

    private val defeated = mutableSetOf<EntityType<*>>()
    val defeatedCount get() = defeated.size

    var maxLevel: Int = if (world.isClient) Int.MAX_VALUE else RPGSkills.CONFIG.baseLevelCap
        private set

    private val increasesLevelCap by lazy {
        world.registryManager[RegistryKeys.ENTITY_TYPE]
            .getEntryList(RPGSkillsTags.INCREASES_LEVEL_CAP)
            .getOrNull()
            ?: RegistryEntryList.empty()
    }

    fun onDefeated(entity: Entity): Boolean {
        if (Registries.ENTITY_TYPE.getEntry(entity.type) !in increasesLevelCap) return false
        if (entity.type in defeated) return false
        defeated.add(entity.type)
        updateLevelCap()
        key.sync(world)

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

    override fun readFromNbt(
        tag: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup
    ) {
        CODEC.update(this, tag).logIfError()
        updateLevelCap()
    }

    override fun writeToNbt(
        tag: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup
    ) {
        CODEC.encode(this, tag).logIfError()
    }

    fun copy(other: BossTrackerComponent) {
        defeated.clear()
        defeated.addAll(other.defeated)
        updateLevelCap()
    }

    companion object : ComponentKeyHolder<BossTrackerComponent, World>, ServerLivingEntityEvents.AfterDeath {
        override val key: ComponentKey<BossTrackerComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("level_cap"), BossTrackerComponent::class.java)

        val CODEC = recordMutationCodec(
            Registries.ENTITY_TYPE.codec.mutateCollection().fieldFor("defeated_bosses", BossTrackerComponent::defeated),
        )

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

        override fun afterDeath(
            entity: LivingEntity,
            damageSource: DamageSource?
        ) {
            entity.world.server?.run {
                val component = overworld[BossTrackerComponent]
                if (!component.onDefeated(entity)) return
                for (world in worlds) {
                    if (world == overworld) continue
                    world[BossTrackerComponent].copy(component)
                }
            }
        }

        @JvmStatic
        fun applyBuffs(entity: LivingEntity) {
            if (entity !is Monster || entity.type isIn RPGSkillsTags.NOT_BUFFED) return

            val defeated = entity.world[BossTrackerComponent].defeatedCount
            if (defeated <= 0) return

            for ((attribute, modifier) in RPGSkills.CONFIG.attributeIncreases) {
                entity.getAttributeInstance(attribute)?.addPersistentModifier(modifier.build(BOSS_DEFEAT_SCALING, defeated.toDouble()))
            }

            if (entity.health < entity.maxHealth)
                entity.health = entity.maxHealth
        }

        fun registerEvents() {
            ServerLivingEntityEvents.AFTER_DEATH.register(this)
        }
    }
}
