package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.condition.IsMonsterLootCondition
import archives.tater.rpgskills.criteria.ItemCraftedCriterion
import archives.tater.rpgskills.criteria.RPGSkillsCriteria
import archives.tater.rpgskills.data.BuildEntry
import archives.tater.rpgskills.data.BuildsRegistry
import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.data.JobProvider
import archives.tater.rpgskills.data.accept
import archives.tater.rpgskills.data.get
import archives.tater.rpgskills.util.*
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.advancement.criterion.ConsumeItemCriterion
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.advancement.criterion.TickCriterion
import net.minecraft.block.Blocks
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityType
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Items
import net.minecraft.loot.condition.BlockStatePropertyLootCondition
import net.minecraft.loot.condition.InvertedLootCondition
import net.minecraft.loot.condition.LocationCheckLootCondition
import net.minecraft.loot.condition.MatchToolLootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.predicate.BlockPredicate
import net.minecraft.predicate.NumberRange
import net.minecraft.predicate.NumberRange.IntRange
import net.minecraft.predicate.entity.EntityEffectPredicate
import net.minecraft.predicate.entity.EntityTypePredicate
import net.minecraft.predicate.entity.LocationPredicate
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.predicate.item.EnchantmentPredicate
import net.minecraft.predicate.item.EnchantmentsPredicate
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.predicate.item.ItemSubPredicateTypes
import net.minecraft.registry.Registerable
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import earth.terrarium.chipped.Chipped
import earth.terrarium.chipped.common.registry.ModBlocks as ChippedBlocks
import net.bettercombat.api.WeaponAttributesHelper.override
import java.util.*
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
        provider.accept(GATHER_WHEAT)
        provider.accept(KILL_POISONED)
        provider.accept(BREED)
        provider.accept(STAND_ON_IRON)
        for (entry in OTHERS)
            provider.accept(entry)
    }

    companion object : BuildsRegistry<Job> {
        override val registry = Job.key

        val PLACE_STONE = depBuildEntry(testPackId("place_stone")) { registerable ->
            Job(
                "Stone Placer",
                mapOf(
                    "place_stone" to Job.Task("Place Stone! you know it's like the gray material the ground is made of", 20, AdvancementCriterion(
                        Criteria.PLACED_BLOCK, itemCriterionConditions(
                            location = LootContextPredicate.create(BlockStatePropertyLootCondition.builder(Blocks.STONE).build())
                        )
                    )),
                    "place_granite" to Job.Task("Break Granite without silk touch. It's the pink rock.", 10, AdvancementCriterion(
                        RPGSkillsCriteria.BREAK_BLOCK, itemCriterionConditions(
                            location = LootContextPredicate.create(
                                BlockStatePropertyLootCondition.builder(Blocks.GRANITE).build(),
                                InvertedLootCondition.builder(
                                    MatchToolLootCondition.builder(ItemPredicate.Builder.create().apply {
                                        subPredicate(
                                            ItemSubPredicateTypes.ENCHANTMENTS,
                                            EnchantmentsPredicate.enchantments(listOf(
                                                EnchantmentPredicate(
                                                    registerable[RegistryKeys.ENCHANTMENT].getOrThrow(Enchantments.SILK_TOUCH),
                                                    IntRange.atLeast(1)
                                                )
                                            ))
                                        )
                                    })
                                ).build()
                            ),
                        )
                    )),
                    "craft_diorite" to Job.Task("Craft any sort of diorite", 24, AdvancementCriterion(
                        RPGSkillsCriteria.CRAFT_ITEM, ItemCraftedCriterion.Conditions(
                            item = ItemPredicate {
                                if (FabricLoader.getInstance().isModLoaded("chipped"))
                                    tag(TagKey.of(RegistryKeys.ITEM, Identifier.of(Chipped.MOD_ID, "diorite")))
                                else
                                    items(
                                        Items.DIORITE,
                                        Items.DIORITE_SLAB,
                                        Items.DIORITE_STAIRS,
                                        Items.DIORITE_WALL,
                                        Items.POLISHED_DIORITE,
                                        Items.POLISHED_DIORITE_SLAB,
                                        Items.POLISHED_DIORITE_STAIRS,
                                    )
                            }
                        )
                    ))
                ),
                rewardPoints = 20,
                cooldownTicks = 20 * 60 * 2,
                spawnAsOrbs = true
            )
        }

        val GATHER_WHEAT = BuildEntry(testPackId("gather_wheat")) {
            Job(
                "Farmin'",
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
                ),
                rewardPoints = 20,
                cooldownTicks = 20 * 60 * 20,
            )
        }

        val KILL_POISONED = BuildEntry(testPackId("kill_poisoned")) {
            Job(
                "Kill Poisoned",
                mapOf(
                    "kill_poisoned" to Job.Task("Kill zombie while poisoned", 4, AdvancementCriterion(
                        Criteria.PLAYER_KILLED_ENTITY, onKilledCriterionConditions(
                            player = EntityPredicate {
                                effects(EntityEffectPredicate.Builder.create().addEffect(StatusEffects.POISON))
                            }.toLootContextPredicate(),
                            entity = LootContextPredicate.create(IsMonsterLootCondition(LootContext.EntityTarget.THIS))
                        )
                    ))
                ),
                rewardPoints = 20,
                cooldownTicks = 20 * 60 * 20,
            )
        }

        val BREED = BuildEntry(testPackId("breed")) {
            Job(
                "Breed",
                mapOf(
                    "breed" to Job.Task("Breed animals", 4, AdvancementCriterion(
                        Criteria.BRED_ANIMALS, bredAnimalsCriterionConditions(
                            parent = EntityPredicate {
                                type(EntityTypePredicate(RegistryEntryList(Registries.ENTITY_TYPE,
                                    EntityType.COW,
                                    EntityType.PIG,
                                    EntityType.SHEEP,
                                    EntityType.CHICKEN,
                                )))
                            }.toLootContextPredicate()
                        )
                    ))
                ),
                rewardPoints = 20,
                cooldownTicks = 20 * 100
            )
        }

        val STAND_ON_IRON = BuildEntry(testPackId("stand_on_iron")) {
            Job(
                "Stand on iron",
                mapOf(
                    "stand" to Job.Task("Stand on iron", 30 * 20, AdvancementCriterion(
                        Criteria.TICK, TickCriterion.Conditions(Optional.of(EntityPredicate {
                            steppingOn(LocationPredicate.Builder.create().apply {
                                block(BlockPredicate.Builder.create().apply {
                                    blocks(Blocks.IRON_BLOCK)
                                })
                            })
                        }.toLootContextPredicate()))
                    ))
                ),
                rewardPoints = 200,
                cooldownTicks = 20 * 30,
            )
        }

        val OTHERS = (1..20).map { BuildEntry(testPackId("other_$it")) {
            Job("Another!", mapOf(
                "thing" to Job.Task("Eat pork", 4, ConsumeItemCriterion.Conditions.item(Items.PORKCHOP))
            ), 1, 20 * 5)
        } }

        override fun bootstrap(registerable: Registerable<Job>) {
            registerable.register(PLACE_STONE)
            registerable.register(GATHER_WHEAT)
            registerable.register(KILL_POISONED)
            registerable.register(BREED)
            registerable.register(STAND_ON_IRON)
            for (entry in OTHERS)
                registerable.register(entry)
        }
    }
}
