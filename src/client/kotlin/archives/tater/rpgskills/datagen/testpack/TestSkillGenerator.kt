package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.RPGSkillsAttributes
import archives.tater.rpgskills.data.BuildsRegistry
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.Skill.AnonymousAttributeModifier
import archives.tater.rpgskills.data.SkillProvider
import archives.tater.rpgskills.data.accept
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
        repeat(5) {
            provider.accept(Identifier.of("rpg_test", "test$it"), Skill(
                icon = ItemStack(Items.IRON_INGOT),
                levels = List(5) { level -> Skill.Level(level + 2)},
                name = "Test $it",
                description = "Things"
            ))
        }
    }

    companion object : BuildsRegistry<Skill> {
        override val registry = Skill.key

        val POTATO_SKILL = BuildEntry(Identifier.of("rpg_test", "potato")) { Skill(
            icon = ItemStack(Items.POTATO),
            levels = listOf(
                Skill.Level(1, mapOf(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED to AnonymousAttributeModifier(0.1, Operation.ADD_MULTIPLIED_BASE),
                )),
                Skill.Level(2, mapOf(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED to AnonymousAttributeModifier(0.1, Operation.ADD_MULTIPLIED_BASE),
                ), listOf(TestJobGenerator.PLACE_STONE.entry)),
                Skill.Level(3),
            ),
            name = "Potato Skill",
            description = "Unlocks potatoes & other stuff"
        ) }

        val COW_SKILL = BuildEntry(Identifier.of("rpg_test", "cow")) {
            Skill(
                icon = ItemStack(Items.COW_SPAWN_EGG),
                levels = listOf(1, 2, 3).map(Skill::Level),
                name = "Cow Skill",
                description = "Unlocks cows I guess"
            )
        }

        val GRASS_SKILL = BuildEntry(Identifier.of("rpg_test", "grass")) {
            Skill(
                icon = ItemStack(Items.GRASS_BLOCK),
                levels = listOf(
                    Skill.Level(1, mapOf(
                        RPGSkillsAttributes.BOW_DRAW_TIME to AnonymousAttributeModifier(-10.0, Operation.ADD_VALUE)
                    )),
                    Skill.Level(2),
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
