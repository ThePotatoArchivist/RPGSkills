package archives.tater.rpgskills.datagen.defaultpack

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.LockGroup.LockList
import archives.tater.rpgskills.data.LockGroupProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class DefaultSkillsLockGenerator(
    dataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : LockGroupProvider(dataOutput, registriesFuture) {
    override fun configure(provider: BiConsumer<Identifier, LockGroup>, registries: RegistryWrapper.WrapperLookup) {
        provider.accept(Identifier.of("rpg_test", "potato1"), LockGroup(
            requirements = mapOf(
                DefaultSkillGenerator.POTATO_SKILL.entry to 1
            ),
            itemName = "Unknown Potato",
            items = LockList(
                Ingredient.ofItems(Items.POTATO, Items.POISONOUS_POTATO),
                "You don't know if this potato is safe to eat",
            ),
            recipes = LockList(
                listOf(Identifier.of("baked_potato"), Identifier.of("baked_potato_from_smoking"), Identifier.of("baked_potato_from_campfire_cooking")),
                "You don't know how to cook this potato",
            ),
        ))
        provider.accept(Identifier.of("rpg_test", "potato2"), LockGroup(
            requirements = mapOf(
                DefaultSkillGenerator.POTATO_SKILL.entry to 2,
                DefaultSkillGenerator.COW_SKILL.entry to 2,
            ),
            itemName = "Unknown Fork",
            items = LockList(
                Ingredient.ofItems(Items.TRIDENT),
                "You don't know how to eat with this fork",
            ),
            recipes = LockList(
                listOf(Identifier.of("prismarine_bricks")),
                "You don't know how to assemble this prismarine",
            ),
        ))
        provider.accept(Identifier.of("rpg_test", "potato3"), LockGroup(
            requirements = listOf(
                mapOf(
                    DefaultSkillGenerator.POTATO_SKILL.entry to 3
                ),
                mapOf(
                    DefaultSkillGenerator.GRASS_SKILL.entry to 2
                ),
            ),
            itemName = "Unknown Button",
            items = LockList(
                Ingredient.fromTag(ItemTags.BUTTONS),
                "You don't know what kind of button this is",
            ),
            recipes = LockList(
                listOf("acacia", "bamboo", "birch", "cherry", "crimson", "jungle", "mangrove", "oak", "spruce", "stone", "warped", "dark_oak", "polished_blackstone").map { Identifier.of("${it}_button") },
                "You don't know how to assemble this button",
            ),
        ))
    }
}
