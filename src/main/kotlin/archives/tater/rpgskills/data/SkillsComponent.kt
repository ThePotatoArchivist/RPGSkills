package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

@Suppress("UnstableApiUsage")
class SkillsComponent(private val player: PlayerEntity) : PlayerComponent<SkillsComponent>, AutoSyncedComponent {
    private var _levels = mutableMapOf<RegistryKey<Skill>, Int>()
    val levels: Map<RegistryKey<Skill>, Int> get() = _levels

    var allowedItems: Ingredient = findAllowedItems()
        private set
    var allowedRecipes: List<Identifier> = findAllowedRecipes()
        private set

    private var _spent = 0
    var remainingLevelPoints
        get() = player.experienceLevel - _spent
        set(value) {
            _spent = player.experienceLevel - value
            key.sync(player)
        }

    operator fun get(skill: RegistryKey<Skill>) = _levels.getOrDefault(skill, 0)
    operator fun set(skill: RegistryKey<Skill>, level: Int) {
        _levels[skill] = level.coerceIn(0, player.world.registryManager[Skill][skill.value]?.levels?.size ?: 1)
        updateAllowed()
        key.sync(player)
    }

    fun getUpgradeCost(skill: RegistryEntry<Skill>): Int? = skill.value.levels
        .getOrNull(this[skill.key.get()])?.cost

    fun getUpgradeCost(skill: RegistryKey<Skill>) = player.world.registryManager[Skill]
        .getEntry(skill).getOrNull()
        ?.let { getUpgradeCost(it) }

    fun canUpgrade(skill: RegistryEntry<Skill>): Boolean = getUpgradeCost(skill)
        ?.let { remainingLevelPoints >= it } ?: false

    fun canUpgrade(skill: RegistryKey<Skill>): Boolean = player.world.registryManager[Skill]
        .getEntry(skill).getOrNull()
        ?.let { canUpgrade(it) } ?: false

    private fun findAllowedItems(): Ingredient = player.world.registryManager[LockGroup]
        .filter { it.isSatisfiedBy(levels) }.map { it.items }
        .let {
            if (it.isEmpty()) Ingredient.EMPTY else DefaultCustomIngredients.any(*it.toTypedArray())
        }

    private fun findAllowedRecipes(): List<Identifier> = player.world.registryManager[LockGroup]
        .filter { it.isSatisfiedBy(levels) }
        .flatMap { it.recipes }

    private fun updateAllowed() {
        allowedItems = findAllowedItems()
        allowedRecipes = findAllowedRecipes()
    }

    override fun shouldCopyForRespawn(lossless: Boolean, keepInventory: Boolean, sameCharacter: Boolean): Boolean =
        sameCharacter

    override fun copyFrom(other: SkillsComponent) {
        _levels = other._levels
        _spent = other._spent
        updateAllowed()
    }

    override fun readFromNbt(tag: NbtCompound) {
        val registry = player.world.registryManager[Skill]
        _levels = tag.getCompound("Levels").run {
            keys.associate { RegistryKey.of(Skill.key, Identifier.tryParse(it)) to getInt(it) }
                .filterKeys { skill -> registry.contains(skill).also {
                    if (!it) RPGSkills.logger.info("Discarding unknown skill {}", skill.value)
                } }
        }.toMutableMap()
        _spent = tag.getInt("Spent")
        updateAllowed()
    }

    override fun writeToNbt(tag: NbtCompound) {
        tag.put("Levels", NbtCompound().apply {
            for ((skill, level) in _levels) {
                putInt(skill.value.toString(), level)
            }
        })
        tag.putInt("Spent", _spent)
    }

    companion object : ComponentKeyHolder<SkillsComponent, PlayerEntity> {
        override val key: ComponentKey<SkillsComponent> = ComponentRegistry.getOrCreate(RPGSkills.id("skills"), SkillsComponent::class.java)

        @JvmField
        val KEY = key
    }
}
