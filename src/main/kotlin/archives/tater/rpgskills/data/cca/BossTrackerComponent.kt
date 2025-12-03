package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.util.*
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.Monster
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.util.Formatting
import net.minecraft.world.World
import net.bettercombat.api.WeaponAttributesHelper.override
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
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

    fun onDefeated(entity: Entity) {
        if (Registries.ENTITY_TYPE.getEntry(entity.type) !in increasesLevelCap) return
        if (entity.type in defeated) return
        defeated.add(entity.type)
        updateLevelCap()
        key.sync(world)

        world.server?.playerManager?.broadcast(
            if (maxLevel < Int.MAX_VALUE)
                CAP_RAISE_MESSAGE.text(entity.name, maxLevel)
            else
                CAP_REMOVED_MESSAGE.text(entity.name),
            false
        )
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

        val CAP_RAISE_MESSAGE = Translation.arg("rpgskills.levelcap.raised") {
            formatted(Formatting.AQUA)
        }
        val CAP_REMOVED_MESSAGE = Translation.arg("rpgskills.levelcap.removed") {
            formatted(Formatting.AQUA)
        }

        val BOSS_DEFEAT_SCALING = RPGSkills.id("boss_defeat_scaling")

        override fun afterDeath(
            entity: LivingEntity,
            damageSource: DamageSource?
        ) {
            entity.world.server?.run {
                val component = overworld[BossTrackerComponent]
                component.onDefeated(entity)
                for (world in worlds) {
                    if (world == overworld) continue
                    world[BossTrackerComponent].copy(component)
                }
            }
        }

        fun applyBuffs(entity: LivingEntity) {
            if (entity !is Monster) return

            val defeated = entity.world[BossTrackerComponent].defeatedCount
            if (defeated <= 0) return

            for ((attribute, modifier) in RPGSkills.CONFIG.attributeIncreases) {
                entity.getAttributeInstance(attribute)?.addPersistentModifier(modifier.build(BOSS_DEFEAT_SCALING, defeated.toDouble()))
            }
        }

        fun registerEvents() {
            ServerLivingEntityEvents.AFTER_DEATH.register(this)
        }
    }
}