@file:JvmName("LockedItems")

package archives.tater.rpgskills.locking

import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillsComponent
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.DynamicRegistryManager
import kotlin.collections.component1
import kotlin.collections.component2

private var locked: Skill.Locked? = null

fun findLocked(registryManager: DynamicRegistryManager) {
    val unlocks = registryManager[Skill].flatMap { skill -> skill.levels.map { it.unlocks } }
    locked = Skill.Locked(
        items = DefaultCustomIngredients.any(*unlocks.map { it.items }.toTypedArray()),
        recipes = unlocks.flatMap { it.recipes }
    )
}

fun isItemLocked(stack: ItemStack, player: PlayerEntity?): Boolean {
    if (player == null) return false
    if (locked == null) findLocked(player.world.registryManager)
    return locked!!.items.test(stack) && !player[SkillsComponent].levels.any { (skill, level) -> skill.value.unlocksItem(level, stack) }
}

@Deprecated("Only for convenience in mixin",
    ReplaceWith("isItemLocked(stack as ItemStack, player)", "net.minecraft.item.ItemStack")
)
internal fun isItemLocked(stack: Any, player: PlayerEntity?) = isItemLocked(stack as ItemStack, player)

fun clearLocked() {
    locked = null
}
