package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.data.*
import archives.tater.rpgskills.util.itemCriterionConditions
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.Blocks
import net.minecraft.loot.condition.BlockStatePropertyLootCondition
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class TestJobGenerator(
    dataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : JobProvider(dataOutput, registriesFuture) {
    override fun configure(
        provider: BiConsumer<Identifier, Job>,
        lookup: RegistryWrapper.WrapperLookup
    ) {
        provider.accept(PLACE_STONE)
    }

    companion object : BuildsRegistry<Job> {
        override val registry = Job.key

        val PLACE_STONE = BuildEntry<Job>(
            Job.key,
            Identifier.of("rpg_test", "place_stone")
        ) {
            Job(
                "Stone Placer",
                "Place stone and granite",
                mapOf(
                    "place_stone" to Job.Task(20, AdvancementCriterion(
                        Criteria.PLACED_BLOCK, itemCriterionConditions(
                            location = LootContextPredicate.create(BlockStatePropertyLootCondition.builder(Blocks.STONE).build())
                        )
                    )),
                    "place_granite" to Job.Task(10, AdvancementCriterion(
                        Criteria.PLACED_BLOCK, itemCriterionConditions(
                            location = LootContextPredicate.create(BlockStatePropertyLootCondition.builder(Blocks.GRANITE).build())
                        )
                    )),
                ), 1, 20 * 30
            )
        }

        override fun bootstrap(registerable: Registerable<Job>) {
            registerable.register(PLACE_STONE)
        }
    }
}
