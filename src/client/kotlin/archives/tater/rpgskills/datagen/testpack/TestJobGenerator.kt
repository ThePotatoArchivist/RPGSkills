package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.data.*
import archives.tater.rpgskills.util.itemCriterionConditions
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.advancement.criterion.ConsumeItemCriterion
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.Blocks
import net.minecraft.loot.condition.BlockStatePropertyLootCondition
import net.minecraft.loot.condition.LocationCheckLootCondition
import net.minecraft.predicate.BlockPredicate
import net.minecraft.predicate.ComponentPredicate
import net.minecraft.predicate.entity.LocationPredicate
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.predicate.item.ItemSubPredicate
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import javax.swing.text.html.Option

class TestJobGenerator(
    dataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : JobProvider(dataOutput, registriesFuture) {
    override fun configure(
        provider: BiConsumer<Identifier, Job>,
        lookup: RegistryWrapper.WrapperLookup
    ) {
        provider.accept(PLACE_STONE)
        provider.accept(GATHER_WHEAT)
        for (job in OTHERS)
            provider.accept(job)
    }

    companion object : BuildsRegistry<Job> {
        override val registry = Job.key

        val PLACE_STONE = BuildEntry(Identifier.of("rpg_test", "place_stone")) {
            Job(
                "Stone Placer",
                "Place stone and granite",
                mapOf(
                    "place_stone" to Job.Task("Place Stone", 20, AdvancementCriterion(
                        Criteria.PLACED_BLOCK, itemCriterionConditions(
                            location = LootContextPredicate.create(BlockStatePropertyLootCondition.builder(Blocks.STONE).build())
                        )
                    )),
                    "place_granite" to Job.Task("Place Granite", 10, AdvancementCriterion(
                        Criteria.PLACED_BLOCK, itemCriterionConditions(
                            location = LootContextPredicate.create(BlockStatePropertyLootCondition.builder(Blocks.GRANITE).build())
                        )
                    )),
                ), 20, 20 * 60 * 2, spawnAsOrbs = true
            )
        }

        val GATHER_WHEAT = BuildEntry(Identifier.of("rpg_test", "gather_wheat")) {
            Job(
                "Farmin'",
                "Tend the farm",
                mapOf(
                    "harvest_crops" to Job.Task("Eat anything", 20, AdvancementCriterion(
                        Criteria.CONSUME_ITEM, ConsumeItemCriterion.Conditions(
                            Optional.empty(),
                            Optional.empty()
                        )
                    )),
                    "plant_crops" to Job.Task("Plant any crop", 20, AdvancementCriterion(
                        Criteria.PLACED_BLOCK, itemCriterionConditions(
                            location = LootContextPredicate.create(LocationCheckLootCondition.builder(
                                LocationPredicate.Builder.create().block(
                                    BlockPredicate.Builder.create().tag(BlockTags.CROPS)
                                )
                            ).build())
                        )
                    ))
                ), 20, 20 * 60 * 20
            )
        }

        val OTHERS = (1..10).map {
            BuildEntry(Identifier.of("rpg_test", "job$it")) {
                Job("Job $it", "EEEEE", mapOf("task" to Job.Task("Place Granite", 10, AdvancementCriterion(
                    Criteria.PLACED_BLOCK, itemCriterionConditions(
                        location = LootContextPredicate.create(BlockStatePropertyLootCondition.builder(Blocks.GRANITE).build())
                    )
                ))), 1, 20)
            }
        }

        override fun bootstrap(registerable: Registerable<Job>) {
            registerable.register(PLACE_STONE)
            registerable.register(GATHER_WHEAT)
            for (job in OTHERS)
                registerable.register(job)
        }
    }
}
