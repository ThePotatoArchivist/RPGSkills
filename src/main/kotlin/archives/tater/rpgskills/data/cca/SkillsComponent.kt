package archives.tater.rpgskills.data.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillClass
import archives.tater.rpgskills.networking.ChooseClassPayload
import archives.tater.rpgskills.networking.ClassChoicePayload
import archives.tater.rpgskills.networking.SkillUpgradePayload
import archives.tater.rpgskills.util.*
import com.google.common.collect.HashMultimap
import com.mojang.serialization.Codec
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryFixedCodec
import net.minecraft.sound.SoundEvents
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import org.ladysnake.cca.api.v3.entity.RespawnableComponent

@Suppress("UnstableApiUsage")
class SkillsComponent(private val player: PlayerEntity) : RespawnableComponent<SkillsComponent>, AutoSyncedComponent {
    private var _skillClass: RegistryEntry<SkillClass>? = null
    var skillClass by ::_skillClass.synced(key, player)

    private var _skills = mutableMapOf<RegistryEntry<Skill>, Int>()
    val skills: Map<RegistryEntry<Skill>, Int>
        get() = _skills

    var level = 0
        private set // managed by _points

    private var _points = 0
        set(value) {
            field = value.coerceAtMost(MAX_POINTS)
            level = getLevelForPoints(value)
        }
    var points by ::_points.synced(key, player)

    private var spentLevels = 0
    var spendableLevels
        get() = level - spentLevels
        set(value) {
            spentLevels = level - value
            key.sync(player)
        }

    val levelProgress get() = getRemainingPoints(points) / getPointsForNextLevel(level).toFloat()

    val isPointsFull get() = level >= MAX_LEVEL

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

    fun resetSkillsToClass() {
        _skills.clear()
        skillClass?.value?.startingLevels?.let(_skills::putAll)
        spentLevels = 0
        updateAttributes()
        key.sync(player)
    }

    private fun getAttributeModifiers(): HashMultimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> =
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
        _skillClass = other._skillClass
        _skills = other._skills
        _points = other.points
        spentLevels = other.spentLevels
        updateAttributes()
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.update(this, tag, registryLookup).logIfError()
        updateAttributes()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        CODEC.encode(this, tag, registryLookup).logIfError()
    }

    companion object : ComponentKeyHolder<SkillsComponent, PlayerEntity> {

        val CODEC = recordMutationCodec(
            RegistryFixedCodec.of(SkillClass.key).optionalFieldOf("class").forAccess(SkillsComponent::_skillClass),
            Codec.unboundedMap(RegistryFixedCodec.of(Skill.key), Codec.INT).mutate().fieldFor("skills", SkillsComponent::_skills),
            Codec.INT.fieldOf("spent").forAccess(SkillsComponent::spentLevels),
            Codec.INT.fieldOf("points").forAccess(SkillsComponent::_points)
        )

        override val key: ComponentKey<SkillsComponent> =
            ComponentRegistry.getOrCreate(RPGSkills.id("skills"), SkillsComponent::class.java)

        const val MAX_LEVEL = 50

        private val LEVEL_REQUIREMENTS = (0..<MAX_LEVEL)
            .runningFold(0) { acc, lvl -> acc + getPointsForNextLevel(lvl) }
        private val LEVEL_REQUIREMENTS_REVERSED = LEVEL_REQUIREMENTS.withIndex().reversed()

        val MAX_POINTS = LEVEL_REQUIREMENTS[MAX_LEVEL]

        /**
         * Matches vanilla XP
         * @return The number of points needed to get to `currentLevel + 1` from `currentLevel`
         */
        fun getPointsForNextLevel(currentLevel: Int) = when {
            currentLevel < 16 -> 2 * currentLevel + 7
            currentLevel < 31 -> 5 * currentLevel - 28
            else -> 9 * currentLevel - 158
        }

        fun getLevelForPoints(points: Int): Int =
            LEVEL_REQUIREMENTS_REVERSED.firstNotNullOfOrNull { (level, required) -> level.takeIf { required <= points } }
                ?: 0

        fun getRemainingPoints(points: Int) =
            points - (LEVEL_REQUIREMENTS_REVERSED.firstOrNull { (_, required) -> required <= points }?.value ?: 0)

        fun registerEvents() {
            // Choose class
            ServerPlayNetworking.registerGlobalReceiver(ClassChoicePayload.ID) { payload, context ->
                val player = context.player()
                val skillsComponent = player[SkillsComponent]
                if (skillsComponent.skillClass != null) {
                    RPGSkills.logger.warn("${player.name.string} tried to set skills class when it was already set")
                    return@registerGlobalReceiver
                }
                skillsComponent.skillClass = payload.skillClass
                skillsComponent.resetSkillsToClass()

                player.interactionManager.gameMode.setAbilities(player.abilities)
                player.sendAbilitiesUpdate()
            }

            // Upgrade skill
            ServerPlayNetworking.registerGlobalReceiver(SkillUpgradePayload.ID) { payload, context ->
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

            // Join without class
            ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
                val player = handler.player
                if (player[SkillsComponent].skillClass != null || server.registryManager[SkillClass].isEmpty()) return@register
                sender.sendPacket(ChooseClassPayload)
                player.abilities.invulnerable = true
                player.sendAbilitiesUpdate()
            }
        }
    }
}

