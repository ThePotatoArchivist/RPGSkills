@file:JvmName("LockedItems")

package archives.tater.rpgskills.locking

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.util.get
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.DynamicRegistryManager

private var locked: LockGroup? = null

fun findLocked(registryManager: DynamicRegistryManager) {
    val unlocks = registryManager[LockGroup]
    locked = LockGroup(
        items = DefaultCustomIngredients.any(*unlocks.map { it.items }.toTypedArray()),
        recipes = unlocks.flatMap { it.recipes }
    )
}

fun isItemLocked(stack: ItemStack, player: PlayerEntity?): Boolean {
    if (player == null) return false
    if (locked == null) findLocked(player.world.registryManager)
    return locked!!.items.test(stack) && !player.world.registryManager[LockGroup].any { group -> group.isSatisfiedBy(player) }
}

@Deprecated("Only for convenience in mixin",
    ReplaceWith("isItemLocked(stack as ItemStack, player)", "net.minecraft.item.ItemStack")
)
internal fun isItemLocked(stack: Any, player: PlayerEntity?) = isItemLocked(stack as ItemStack, player)

fun clearLocked() {
    locked = null
}
