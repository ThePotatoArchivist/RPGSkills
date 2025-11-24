package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsTags
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.encode
import archives.tater.rpgskills.util.fieldFor
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.logIfError
import archives.tater.rpgskills.util.mutateCollection
import archives.tater.rpgskills.util.recordMutationCodec
import archives.tater.rpgskills.util.update
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.world.World
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import kotlin.jvm.optionals.getOrNull
import kotlin.run

class LevelCapComponent(private val world: World) : Component, AutoSyncedComponent {

    private val defeated = mutableSetOf<EntityType<*>>()
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
    }

    private fun updateLevelCap() {
        maxLevel = if (defeated.size >= increasesLevelCap.size())
            Int.MAX_VALUE
        else
            RPGSkills.CONFIG.baseLevelCap + RPGSkills.CONFIG.levelCapIncreasePerBoss * defeated.size
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

    fun copy(other: LevelCapComponent) {
        defeated.clear()
        defeated.addAll(other.defeated)
        updateLevelCap()
    }

    companion object : ComponentKeyHolder<LevelCapComponent, World>, ServerLivingEntityEvents.AfterDeath {
        override val key: ComponentKey<LevelCapComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("level_cap"), LevelCapComponent::class.java)

        val CODEC = recordMutationCodec(
            Registries.ENTITY_TYPE.codec.mutateCollection().fieldFor("defeated_bosses", LevelCapComponent::defeated),
        )

        override fun afterDeath(
            entity: LivingEntity,
            damageSource: DamageSource?
        ) {
            entity.world.server?.run {
                val component = overworld[LevelCapComponent]
                component.onDefeated(entity)
                for (world in worlds) {
                    if (world == overworld) continue
                    world[LevelCapComponent].copy(component)
                }
            }

        }

        fun registerEvents() {
            ServerLivingEntityEvents.AFTER_DEATH.register(this)
        }
    }
}