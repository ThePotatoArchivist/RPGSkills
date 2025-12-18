package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.LockGroup.LockList
import archives.tater.rpgskills.data.LockGroupProvider
import archives.tater.rpgskills.data.RegistryIngredient
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.block.Blocks
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityType
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
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
                RegistryIngredient.ofItems {
                    +Items.BAKED_POTATO
                },
                "You don't know how to cook this potato",
            ),
        ))
        provider.accept(testPackId("potato2"), LockGroup(
            requirements = mapOf(
                TestSkillGenerator.POTATO_SKILL.entry to 2,
                TestSkillGenerator.COW_SKILL.entry to 2,
            ),
            items = LockList(
                RegistryIngredient.ofItems {
                    +Items.TRIDENT
                    +Items.DIAMOND_HELMET
                    +Items.COD
                    +Items.CHEST_MINECART
                    +Items.ARMOR_STAND
                },
                "You don't know how to eat with this fork",
            ),
            entities = LockList(
                RegistryIngredient.ofEntities {
                    +EntityType.VILLAGER
                    +EntityType.CHICKEN
                    +EntityType.ARMOR_STAND
                    +EntityType.TNT_MINECART
                },
                "AAA"
            ),
            enchantments = LockList(
                RegistryIngredient.of(registries, RegistryKeys.ENCHANTMENT) {
                    +Enchantments.AQUA_AFFINITY
                }
            ),
            recipes = LockList(
                RegistryIngredient.ofItems {
                    +Items.PRISMARINE_BRICKS
                },
                "You don't know how to assemble this prismarine",
            ),
        ))
        provider.accept(testPackId("potato3"), LockGroup(
            requirements = listOf(
                mapOf(
                    TestSkillGenerator.COW_SKILL.entry to 1,
                    TestSkillGenerator.POTATO_SKILL.entry to 3,
                ),
                mapOf(
                    TestSkillGenerator.COW_SKILL.entry to 1,
                    TestSkillGenerator.GRASS_SKILL.entry to 2,
                ),
            ),
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
                RegistryIngredient.ofItems {
                    +ItemTags.BUTTONS
                },
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
