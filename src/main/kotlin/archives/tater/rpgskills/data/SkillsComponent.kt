package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.networking.SkillUpgradePayload
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.associateNotNull
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import com.google.common.collect.HashMultimap
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import org.ladysnake.cca.api.v3.entity.RespawnableComponent
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.jvm.optionals.getOrNull

typealias ModifierMap = HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier>

@Suppress("UnstableApiUsage")
class SkillsComponent(private val player: PlayerEntity) : RespawnableComponent<SkillsComponent>, AutoSyncedComponent {
    private var _levels = mutableMapOf<RegistryEntry<Skill>, Int>()
    val levels: Map<RegistryEntry<Skill>, Int> get() = _levels

    private var _spent = 0
    var remainingLevelPoints
        get() = player.experienceLevel - _spent
        set(value) {
            _spent = player.experienceLevel - value
            key.sync(player)
        }

    private var modifiers: ModifierMap = HashMultimap.create()

    operator fun get(skill: RegistryEntry<Skill>) = _levels.getOrDefault(skill, 0)
    operator fun set(skill: RegistryEntry<Skill>, level: Int) {
        _levels[skill] = level.coerceIn(0, skill.value.levels.size)
        updateAttributes()
        key.sync(player)
    }

    fun getUpgradeCost(skill: RegistryEntry<Skill>): Int? = skill.value.levels
        .getOrNull(this[skill])?.cost

    fun canUpgrade(skill: RegistryEntry<Skill>): Boolean = getUpgradeCost(skill)
        ?.let { remainingLevelPoints >= it } ?: false

    private fun getAttributeModifiers() =
        HashMultimap.create<RegistryEntry<EntityAttribute>, EntityAttributeModifier>().apply {
            for ((skill, playerLevel) in _levels)
                skill.value.levels
                    .filter { playerLevel >= it.cost }
                    .forEachIndexed { levelIndex, level ->
                        for ((attribute, modifier) in level.attributes)
                            put(attribute, modifier.build(skill.key.orElseThrow().value.withPath { "skill/$it/$levelIndex" }))
                    }
        }

    private fun updateAttributes() {
        if (player.world.isClient) return
        player.attributes.removeModifiers(modifiers)
        modifiers = getAttributeModifiers().also {
            player.attributes.addTemporaryModifiers(it)
        }
    }

    override fun shouldCopyForRespawn(lossless: Boolean, keepInventory: Boolean, sameCharacter: Boolean): Boolean =
        sameCharacter

    override fun copyFrom(other: SkillsComponent, registryLookup: RegistryWrapper.WrapperLookup) {
        _levels = other._levels
        _spent = other._spent
        updateAttributes()
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        val registry = registryLookup[Skill]
        _levels = tag.getCompound("Levels").run {
            keys.associateNotNull { key ->
                Identifier.tryParse(key)
                    ?.let { registry.getOptional(RegistryKey.of(Skill.key, it)).getOrNull() }
                    ?.let { it to getInt(key) }
            }
        }.toMutableMap()
        _spent = tag.getInt("Spent")
        updateAttributes()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.put("Levels", NbtCompound().apply {
            for ((skill, level) in _levels) {
                putInt(skill.key.get().value.toString(), level)
            }
        })
        tag.putInt("Spent", _spent)
    }

    companion object : ComponentKeyHolder<SkillsComponent, PlayerEntity>, PlayPayloadHandler<SkillUpgradePayload> {
        override val key: ComponentKey<SkillsComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("skills"), SkillsComponent::class.java)

        @JvmField
        val KEY = key

        override fun receive(payload: SkillUpgradePayload, context: ServerPlayNetworking.Context) {
            val player = context.player()
            val skillsComponent = player[SkillsComponent]
            val skill = payload.skill
            if (skillsComponent.canUpgrade(skill)) {
                skillsComponent.remainingLevelPoints -= skillsComponent.getUpgradeCost(skill)!!
                skillsComponent[skill]++
                player.world.playSoundFromEntity(null, player, SoundEvents.ENTITY_PLAYER_LEVELUP, player.soundCategory, 1f, 1f)
            }
        }

        fun registerNetworking() {
            ServerPlayNetworking.registerGlobalReceiver(SkillUpgradePayload.ID, this)
        }
    }
}
