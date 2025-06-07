package archives.tater.rpgskills.item

import archives.tater.rpgskills.RPGSkills
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Rarity

object RPGSkillsItems {
    private fun register(path: String, item: Item): Item = Registry.register(Registries.ITEM, RPGSkills.id(path), item)

    private fun itemSettings(init: Item.Settings.() -> Unit) = Item.Settings().apply(init)

    val RESPEC_ITEM = register("respec_item", RespecItem(itemSettings {
        maxCount(1)
        fireproof()
        rarity(Rarity.RARE)
    }))

    fun register() {}
}