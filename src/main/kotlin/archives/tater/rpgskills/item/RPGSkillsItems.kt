package archives.tater.rpgskills.item

import archives.tater.rpgskills.RPGSkills
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Rarity

object RPGSkillsItems {
    private fun register(path: String, item: Item): Item = Registry.register(Registries.ITEM, RPGSkills.id(path), item)

    private fun itemSettings(init: Item.Settings.() -> Unit) = Item.Settings().apply(init)

    val REBIRTH_ELIXIR = register("rebirth_elixir", RespecItem(itemSettings {
        maxCount(1)
        fireproof()
        rarity(Rarity.RARE)
    }))

    val SKILL_NUGGET = register("skill_nugget", SkillNuggetItem(Item.Settings()))

    fun register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register { entries ->
            entries.addAfter(Items.EXPERIENCE_BOTTLE, REBIRTH_ELIXIR, SKILL_NUGGET)
        }
    }
}