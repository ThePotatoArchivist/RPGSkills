package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.cca.SkillsComponent.PointsChangedCallback
import archives.tater.rpgskills.networking.SkillUpgradePayload
import archives.tater.rpgskills.util.*
import com.google.common.collect.HashMultimap
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
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
import kotlin.jvm.optionals.getOrNull

@Suppress("UnstableApiUsage")
class SkillsComponent(private val player: PlayerEntity) : RespawnableComponent<SkillsComponent>, AutoSyncedComponent {
    private var _skills = mutableMapOf<RegistryEntry<Skill>, Int>()
    val skills: Map<RegistryEntry<Skill>, Int> get() = _skills

    private var _points = 0
    var points by ::_points.synced(key, player)

    val level
        get() = getLevelForPoints(points)

    private var spentLevels = 0
    var spendableLevels
        get() = level - spentLevels
        set(value) {
            spentLevels = level - value
            key.sync(player)
        }

    val levelProgress get() = getRemainingPoints(points) / getPointsForNextLevel(level + 1).toFloat()

    private var modifiers: HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> =
        HashMultimap.create()

    operator fun get(skill: RegistryEntry<Skill>) = _skills.getOrDefault(skill, 0)
    operator fun set(skill: RegistryEntry<Skill>, level: Int) {
        _skills[skill] = level.coerceIn(0, skill.value.levels.size)
        updateAttributes()
        key.sync(player)
    }

    fun getUpgradeCost(skill: RegistryEntry<Skill>): Int? = skill.value.levels
        .getOrNull(this[skill])?.cost

    fun canUpgrade(skill: RegistryEntry<Skill>): Boolean = getUpgradeCost(skill)
        ?.let { spendableLevels >= it } ?: false

    private fun getAttributeModifiers() =
        HashMultimap.create<RegistryEntry<EntityAttribute>, EntityAttributeModifier>().apply {
            for ((skill, playerLevel) in _skills)
                skill.value.levels
                    .filter { playerLevel >= it.cost }
                    .forEachIndexed { levelIndex, level ->
                        for ((attribute, modifier) in level.attributes)
                            put(
                                attribute,
                                modifier.build(skill.key.orElseThrow().value.withPath { "skill/$it/$levelIndex" })
                            )
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
        _points = other.points
        spentLevels = other.spentLevels
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
        spentLevels = tag.getInt("spent")
        _points = tag.getInt("points")
        updateAttributes()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.put("skills", NbtCompound().apply {
            for ((skill, level) in _skills) {
                putInt(skill.key.get().value.toString(), level)
            }
        })
        tag.putInt("spent", spentLevels)
        tag.putInt("points", _points)
    }

    override fun applySyncPacket(buf: RegistryByteBuf) {
        val prevPoints = points
        val prevLevel = level
        super.applySyncPacket(buf)
        PointsChangedCallback.EVENT.invoker().onChange(player, this, prevPoints, points, prevLevel, level)
    }

    companion object : ComponentKeyHolder<SkillsComponent, PlayerEntity>,
        ServerPlayNetworking.PlayPayloadHandler<SkillUpgradePayload> {
        override val key: ComponentKey<SkillsComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("skills"), SkillsComponent::class.java)

        @JvmField
        val KEY = key

        private val LEVEL_REQUIREMENTS = (0..100)
            .runningFold(0) { acc, lvl -> acc + getPointsForNextLevel(lvl) }
        private val LEVEL_REQUIREMENTS_REVERSED = LEVEL_REQUIREMENTS.withIndex().reversed()

        /**
         * @return The number of points needed to get to `level` from `level - 1`
         */
        fun getPointsForNextLevel(nextLevel: Int) = 10 + (nextLevel - 1) // TODO

        fun getLevelForPoints(points: Int): Int =
            LEVEL_REQUIREMENTS_REVERSED.firstNotNullOfOrNull { (level, required) -> level.takeIf { required < points } }
                ?: 0

        fun getRemainingPoints(points: Int) =
            points - (LEVEL_REQUIREMENTS_REVERSED.firstOrNull { (_, required) -> required < points }?.value ?: 0)

        override fun receive(payload: SkillUpgradePayload, context: ServerPlayNetworking.Context) {
            val player = context.player()
            val skillsComponent = player[SkillsComponent]
            val skill = payload.skill
            if (skillsComponent.canUpgrade(skill)) {
                skillsComponent.spendableLevels -= skillsComponent.getUpgradeCost(skill)!!
                skillsComponent[skill]++
                player.world.playSoundFromEntity(
                    null, player,
                    SoundEvents.ENTITY_PLAYER_LEVELUP, player.soundCategory, 1f, 1f
                )
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
        fun onChange(
            player: PlayerEntity,
            skills: SkillsComponent,
            prevPoints: Int,
            newPoints: Int,
            prevLevel: Int,
            newLevel: Int
        )

        companion object {
            val EVENT: Event<PointsChangedCallback> =
                EventFactory.createArrayBacked(PointsChangedCallback::class.java) { callbacks ->
                    PointsChangedCallback { player, skills, prevPoints, newPoints, prevLevel, newLevel ->
                        for (callback in callbacks)
                            callback.onChange(player, skills, prevPoints, newPoints, prevLevel, newLevel)
                    }
                }
        }
    }
}

