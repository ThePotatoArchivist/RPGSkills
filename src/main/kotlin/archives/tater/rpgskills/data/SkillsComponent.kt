package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.ComponentKeyHolder
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.registryOf
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import org.ladysnake.cca.api.v3.entity.RespawnableComponent
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@Suppress("UnstableApiUsage")
class SkillsComponent(private val player: PlayerEntity) : RespawnableComponent<SkillsComponent>, AutoSyncedComponent {
    private var _levels = mutableMapOf<RegistryEntry<Skill>, Int>()
    val levels: Map<RegistryEntry<Skill>, Int> get() = _levels

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

    operator fun get(skill: RegistryEntry<Skill>) = _levels.getOrDefault(skill, 0)
    operator fun set(skill: RegistryEntry<Skill>, level: Int) {
        _levels[skill] = level.coerceIn(0, skill.value.levels.size)
        updateAllowed()
        key.sync(player)
    }

    fun getUpgradeCost(skill: RegistryEntry<Skill>): Int? = skill.value.levels
        .getOrNull(this[skill])?.cost

    fun canUpgrade(skill: RegistryEntry<Skill>): Boolean = getUpgradeCost(skill)
        ?.let { remainingLevelPoints >= it } ?: false

    private fun findAllowedItems(): Ingredient = registryOf(player, LockGroup)
        .filter { it.isSatisfiedBy(levels) }.map { it.items }
        .let {
            if (it.isEmpty()) Ingredient.EMPTY else DefaultCustomIngredients.any(*it.toTypedArray())
        }

    private fun findAllowedRecipes(): List<Identifier> = registryOf(player, LockGroup)
        .filter { it.isSatisfiedBy(levels) }
        .flatMap { it.recipes }

    private fun updateAllowed() {
        allowedItems = findAllowedItems()
        allowedRecipes = findAllowedRecipes()
    }

    override fun shouldCopyForRespawn(lossless: Boolean, keepInventory: Boolean, sameCharacter: Boolean): Boolean =
        sameCharacter

    override fun copyFrom(other: SkillsComponent, registryLookup: RegistryWrapper.WrapperLookup) {
        _levels = other._levels
        _spent = other._spent
        updateAllowed()
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        val registry = registryLookup[Skill]
        _levels = tag.getCompound("Levels").run {
            // TODO catch errors here
            keys.associate { registry.getOrThrow(RegistryKey.of(Skill.key, Identifier.tryParse(it))) to getInt(it) }
        }.toMutableMap()
        _spent = tag.getInt("Spent")
        updateAllowed()
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.put("Levels", NbtCompound().apply {
            for ((skill, level) in _levels) {
                putInt(skill.key.get().value.toString(), level)
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
