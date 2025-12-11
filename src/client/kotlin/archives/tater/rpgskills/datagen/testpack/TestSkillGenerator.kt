package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.RPGSkillsAttributes
import archives.tater.rpgskills.data.*
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registerable
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class TestSkillGenerator(
    dataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : SkillProvider(dataOutput, registriesFuture) {
    override fun configure(provider: BiConsumer<Identifier, Skill>, lookup: RegistryWrapper.WrapperLookup) {
		provider.accept(POTATO_SKILL)
        provider.accept(COW_SKILL)
        provider.accept(GRASS_SKILL)
        repeat(3) {
            provider.accept(testPackId("test$it"), Skill(
                icon = Items.IRON_INGOT.defaultStack,
                levels = List(5) { Skill.Level() },
                name = "Test $it",
                description = "Things"
            ))
        }
        provider.accept(testPackId("test_many"), Skill(
            icon = Items.STRING.defaultStack,
            levels = List(30) { Skill.Level() },
            name = "Long test",
            description = "It's very long"
        ))
    }

    companion object : BuildsRegistry<Skill> {
        override val registry = Skill.key

        val POTATO_SKILL = depBuildEntry(testPackId("potato")) { registerable ->
            Skill(
                icon = ItemStack(Items.IRON_SWORD),
                levels = listOf(
                    Skill.Level(mapOf(
                        EntityAttributes.GENERIC_MOVEMENT_SPEED to AnonymousAttributeModifier(0.1, Operation.ADD_MULTIPLIED_BASE),
                    )),
                    Skill.Level(mapOf(
                        EntityAttributes.GENERIC_MOVEMENT_SPEED to AnonymousAttributeModifier(0.1, Operation.ADD_MULTIPLIED_BASE),
                    ), listOf(registerable[TestJobGenerator.PLACE_STONE])),
                    Skill.Level(jobs = TestJobGenerator.OTHERS.map { registerable[it] }),
                ),
                name = "Affinity",
                description = "Unlocks potatoes & other stuff"
            ) 
        }

        val COW_SKILL = depBuildEntry(testPackId("cow")) { registerable ->
            Skill(
                icon = ItemStack(Items.COW_SPAWN_EGG),
                levels = listOf(
                    Skill.Level(jobs = listOf(registerable[TestJobGenerator.KILL_POISONED])),
                    Skill.Level(jobs = listOf(registerable[TestJobGenerator.BREED])),
                    Skill.Level(jobs = listOf(registerable[TestJobGenerator.GATHER_WHEAT]))
                ),
                name = "Cow Skill",
                description = "Unlocks cows I guess"
            )
        }

        val GRASS_SKILL = depBuildEntry(testPackId("grass")) { registerable ->
            Skill(
                icon = ItemStack(Items.GRASS_BLOCK),
                levels = listOf(
                    Skill.Level(mapOf(
                        RPGSkillsAttributes.BOW_DRAW_TIME to AnonymousAttributeModifier(-10.0, Operation.ADD_VALUE)
                    )),
                    Skill.Level(jobs = listOf(registerable[TestJobGenerator.STAND_ON_IRON])),
                ),
                name = "Grass Skill",
                description = "Unlocks cows I guess"
            )
        }

        override fun bootstrap(registerable: Registerable<Skill>) {
            registerable.register(POTATO_SKILL)
            registerable.register(COW_SKILL)
            registerable.register(GRASS_SKILL)
        }
    }
}
