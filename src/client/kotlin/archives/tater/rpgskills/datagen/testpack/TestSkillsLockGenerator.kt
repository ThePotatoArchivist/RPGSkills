package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.LockGroup.LockList
import archives.tater.rpgskills.data.LockGroupProvider
import archives.tater.rpgskills.data.RegistryIngredient
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.item.Items
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class TestSkillsLockGenerator(
    dataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : LockGroupProvider(dataOutput, registriesFuture) {
    override fun configure(provider: BiConsumer<Identifier, LockGroup>, registries: RegistryWrapper.WrapperLookup) {
        provider.accept(testPackId("potato1"), LockGroup(
            requirements = mapOf(
                TestSkillGenerator.POTATO_SKILL.entry to 1
            ),
            itemName = "Unknown Potato",
            items = LockList(
                RegistryIngredient.ofItems {
                    +Items.POTATO
                    +Items.POISONOUS_POTATO
                },
                "You don't know if this potato is safe to eat",
            ),
            blocks = LockList(
                RegistryIngredient.ofBlocks {
                    +Blocks.CRAFTING_TABLE
                },
            ),
            recipes = LockList(
                listOf(Identifier.of("baked_potato"), Identifier.of("baked_potato_from_smoking"), Identifier.of("baked_potato_from_campfire_cooking")),
                "You don't know how to cook this potato",
            ),
        ))
        provider.accept(testPackId("potato2"), LockGroup(
            requirements = mapOf(
                TestSkillGenerator.POTATO_SKILL.entry to 2,
                TestSkillGenerator.COW_SKILL.entry to 2,
            ),
            itemName = "Unknown Fork",
            items = LockList(
                RegistryIngredient.ofItems {
                    +Items.TRIDENT
                    +Items.DIAMOND_HELMET
                    +Items.COD
                },
                "You don't know how to eat with this fork",
            ),
            entities = LockList(
                RegistryIngredient.ofEntities {
                    +EntityType.VILLAGER
                },
                "AAA"
            ),
            recipes = LockList(
                listOf(Identifier.of("prismarine_bricks")),
                "You don't know how to assemble this prismarine",
            ),
        ))
        provider.accept(testPackId("potato3"), LockGroup(
            requirements = listOf(
                mapOf(
                    TestSkillGenerator.POTATO_SKILL.entry to 3
                ),
                mapOf(
                    TestSkillGenerator.GRASS_SKILL.entry to 2
                ),
            ),
            itemName = "Unknown Button",
            items = LockList(
                RegistryIngredient.ofItems {
                    +ItemTags.BUTTONS
                    +Items.RED_CONCRETE
                },
                "You don't know what kind of button this is",
            ),
            blocks = LockList(
                RegistryIngredient.ofBlocks {
                    +BlockTags.BUTTONS
                },
                "How to press?"
            ),
            recipes = LockList(
                listOf("acacia", "bamboo", "birch", "cherry", "crimson", "jungle", "mangrove", "oak", "spruce", "stone", "warped", "dark_oak", "polished_blackstone").map { Identifier.of("${it}_button") },
                "You don't know how to assemble this button",
            ),
        ))
        provider.accept(testPackId("potato4"), LockGroup(
            requirements = mapOf(
                TestSkillGenerator.POTATO_SKILL.entry to 2,
            ),
            items = LockList(
                RegistryIngredient.ofItems {
                    +Items.BARRIER
                },
            ),
        ))
    }
}
