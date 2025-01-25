@file:JvmName("LockedItems")

package archives.tater.rpgskills.data

import archives.tater.rpgskills.util.get
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries

private var locked: Skill.Locked? = null

private var lockedItems: Set<Item>? = null

@Suppress("DEPRECATION")
fun findLocked(registryManager: DynamicRegistryManager) {
    val unlocks = registryManager[Skill].flatMap { skill -> skill.levels.map { it.unlocks } }
    locked = Skill.Locked(
        items = unlocks.flatMap { it.items },
        tags = unlocks.flatMap { it.tags },
        recipes = unlocks.flatMap { it.recipes }
    ).also {
        lockedItems = mutableSetOf<Item>().apply {
            addAll(it.items)
            addAll(Registries.ITEM.filter { item ->
                it.tags.any { tag ->
                    item.registryEntry.isIn(tag)
                }
            })
        }
    }
}

fun isItemLocked(item: Item, player: PlayerEntity): Boolean {
    if (locked == null) findLocked(player.world.registryManager)
    return item in lockedItems!!
}

fun clearLocked() {
    locked = null
    lockedItems = null
}
