@file:JvmName("LockedItems")

package archives.tater.rpgskills.data

import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.DynamicRegistryManager

private var locked: Skill.Locked? = null

fun findLocked(registryManager: DynamicRegistryManager) {
    val unlocks = registryManager[Skill].flatMap { skill -> skill.levels.map { it.unlocks } }
    locked = Skill.Locked(
        items = DefaultCustomIngredients.any(*unlocks.map { it.items }.toTypedArray()),
        recipes = unlocks.flatMap { it.recipes }
    )
}

fun isItemLocked(stack: ItemStack, player: PlayerEntity): Boolean {
    if (locked == null) findLocked(player.world.registryManager)
    return locked!!.items.test(stack) && !player[SkillsComponent].levels.any { (skill, level) -> skill.value.unlocksItem(level, stack) }
}

/**
 * Only for use in [archives.tater.rpgskills.mixin.ItemStackMixin]
 */
fun isItemLocked(stack: Any, player: PlayerEntity) = isItemLocked(stack as ItemStack, player)

fun clearLocked() {
    locked = null
}
