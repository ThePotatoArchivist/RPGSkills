package archives.tater.rpgskills.datagen.defaultpack

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.LockGroupProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import java.util.function.BiConsumer

class DefaultSkillsLockGenerator(dataOutput: FabricDataOutput) : LockGroupProvider(dataOutput) {
    override fun configure(provider: BiConsumer<Identifier, LockGroup>) {
        provider.accept(Identifier("rpg_test", "potato1"), LockGroup(
            items = Ingredient.ofItems(Items.POTATO, Items.POISONOUS_POTATO),
            recipes = listOf(Identifier("baked_potato"), Identifier("baked_potato_from_smoking"), Identifier("baked_potato_from_campfire_cooking")),
            requirements = mapOf(
                DefaultSkillGenerator.POTATO_SKILL.key to 1
            ),
            itemName = "Unknown Potato",
            itemMessage = "You don't know if this potato is safe to eat",
            recipeMessage = "You don't know how to cook this potato",
        ))
        provider.accept(Identifier("rpg_test", "potato2"), LockGroup(
            items = Ingredient.ofItems(Items.TRIDENT),
            recipes = listOf(Identifier("prismarine_bricks")),
            requirements = mapOf(
                DefaultSkillGenerator.POTATO_SKILL.key to 2
            ),
            itemName = "Unknown Fork",
            itemMessage = "You don't know how to eat with this fork",
            recipeMessage = "You don't know how to assemble this prismarine",
        ))
        provider.accept(Identifier("rpg_test", "potato3"), LockGroup(
            items = Ingredient.fromTag(ItemTags.BUTTONS),
            recipes = listOf("acacia", "bamboo", "birch", "cherry", "crimson", "jungle", "mangrove", "oak", "spruce", "stone", "warped", "dark_oak", "polished_blackstone").map { Identifier("${it}_button") },
            requirements = mapOf(
                DefaultSkillGenerator.POTATO_SKILL.key to 3
            ),
            itemName = "Unknown Button",
            itemMessage = "You don't know what kind of button this is",
            recipeMessage = "You don't know how to assemble this button",
        ))
    }
}
