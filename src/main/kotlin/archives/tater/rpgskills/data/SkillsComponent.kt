package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.SkillsComponent.PointsChangedCallback
import archives.tater.rpgskills.networking.SkillUpgradePayload
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.associateNotNull
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import com.google.common.collect.HashMultimap
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.RegistryByteBuf
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
    private var _skills = mutableMapOf<RegistryEntry<Skill>, Int>()
    val skills: Map<RegistryEntry<Skill>, Int> get() = _skills

    private var _points = 0
    var points
        get() = _points
        set(value) {
            var newPoints = value
            var newLevel = _level
            while (newPoints < 0) {
                newPoints += getPointsForLevel(newLevel)
                newLevel--
            }
            while (newPoints > getPointsForLevel(newLevel + 1)) {
                newPoints -= getPointsForLevel(newLevel + 1)
                newLevel++
            }
            PointsChangedCallback.EVENT.invoker().onChange(player, this, points, newPoints, level, newLevel)
            _points = newPoints
            _level = newLevel
            key.sync(player)
        }

    private var _level = 0
    var level
        get() = _level
        set(value) {
            val newPoints = _points * getPointsForLevel(value) / getPointsForLevel(_level + 1)
            PointsChangedCallback.EVENT.invoker().onChange(player, this, points, newPoints, _level, value)
            _points = newPoints
            _level = value
            key.sync(player)
        }

    val levelProgress get() = points / getPointsForLevel(level + 1).toFloat()

    private var modifiers: ModifierMap = HashMultimap.create()

    operator fun get(skill: RegistryEntry<Skill>) = _skills.getOrDefault(skill, 0)
    operator fun set(skill: RegistryEntry<Skill>, level: Int) {
        _skills[skill] = level.coerceIn(0, skill.value.levels.size)
        updateAttributes()
        key.sync(player)
    }

    fun getUpgradeCost(skill: RegistryEntry<Skill>): Int? = skill.value.levels
        .getOrNull(this[skill])?.cost

    fun canUpgrade(skill: RegistryEntry<Skill>): Boolean = getUpgradeCost(skill)
        ?.let { level >= it } ?: false

    private fun getAttributeModifiers() =
        HashMultimap.create<RegistryEntry<EntityAttribute>, EntityAttributeModifier>().apply {
            for ((skill, playerLevel) in _skills)
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
        _skills = other._skills
        _level = other._level
        updateAttributes()
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        val registry = registryLookup[Skill]
        _skills = tag.getCompound("skills").run {
            keys.associateNotNull { key ->
                Identifier.tryParse(key)
                    ?.let { registry.getOptional(RegistryKey.of(Skill.key, it)).getOrNull() }
                    ?.let { it to getInt(key) }
            }
        }.toMutableMap()
        _level = tag.getInt("levels")
        _points = tag.getInt("points")
        updateAttributes()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.put("skills", NbtCompound().apply {
            for ((skill, level) in _skills) {
                putInt(skill.key.get().value.toString(), level)
            }
        })
        tag.putInt("levels", _level)
        tag.putInt("points", _points)
    }

    override fun applySyncPacket(buf: RegistryByteBuf) {
        val prevPoints = points
        val prevLevel = level
        super.applySyncPacket(buf)
        PointsChangedCallback.EVENT.invoker().onChange(player, this, prevPoints, points, prevLevel, level)
    }

    companion object : ComponentKeyHolder<SkillsComponent, PlayerEntity>, PlayPayloadHandler<SkillUpgradePayload> {
        override val key: ComponentKey<SkillsComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("skills"), SkillsComponent::class.java)

        @JvmField
        val KEY = key

        /**
         * @return The number of points needed to get to `level` from `level - 1`
         */
        fun getPointsForLevel(level: Int) = 10 + (level - 1) // TODO

        override fun receive(payload: SkillUpgradePayload, context: ServerPlayNetworking.Context) {
            val player = context.player()
            val skillsComponent = player[SkillsComponent]
            val skill = payload.skill
            if (skillsComponent.canUpgrade(skill)) {
                skillsComponent.level -= skillsComponent.getUpgradeCost(skill)!!
                skillsComponent[skill]++
                player.world.playSoundFromEntity(null, player, SoundEvents.ENTITY_PLAYER_LEVELUP, player.soundCategory, 1f, 1f)
            }
        }

        fun registerNetworking() {
            ServerPlayNetworking.registerGlobalReceiver(SkillUpgradePayload.ID, this)
        }
    }

    /**
     * Called whenever levels or points are changed. This is called before the actual fields are updated.
     */
    fun interface PointsChangedCallback {
        fun onChange(player: PlayerEntity, skills: SkillsComponent, prevPoints: Int, newPoints: Int, prevLevel: Int, newLevel: Int)

        companion object {
            val EVENT: Event<PointsChangedCallback> = EventFactory.createArrayBacked(PointsChangedCallback::class.java) { callbacks ->
                PointsChangedCallback { player, skills, prevPoints, newPoints, prevLevel, newLevel ->
                    for (callback in callbacks)
                        callback.onChange(player, skills, prevPoints, newPoints, prevLevel, newLevel)
                }
            }
        }
    }
}
