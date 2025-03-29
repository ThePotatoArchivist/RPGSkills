package archives.tater.rpgskills.datagen.defaultpack

import archives.tater.rpgskills.data.BuildsRegistry
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillProvider
import archives.tater.rpgskills.data.accept
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registerable
import net.minecraft.util.Identifier
import java.util.function.BiConsumer

class DefaultSkillGenerator(dataOutput: FabricDataOutput) : SkillProvider(dataOutput) {
	override fun configure(provider: BiConsumer<Identifier, Skill>) {
		provider.accept(POTATO_SKILL)
        provider.accept(BuildEntry(Identifier("rpg_test", "cow"), Skill(
            icon = ItemStack(Items.COW_SPAWN_EGG),
            levels = listOf(),
            name = "Cow Skill",
            description = "Unlocks cows I guess"
        )))
        provider.accept(BuildEntry(Identifier("rpg_test", "grass"), Skill(
            icon = ItemStack(Items.GRASS_BLOCK),
            levels = listOf(),
            name = "Grass Skill",
            description = "Unlocks cows I guess"
        )))
        repeat(5) {
            provider.accept(BuildEntry(Identifier("rpg_test", "test$it"), Skill(
                icon = ItemStack(Items.IRON_INGOT),
                levels = List(5) { level -> Skill.Level(level + 2)},
                name = "Test $it",
                description = "Things"
            )))
        }
    }

    companion object : BuildsRegistry<Skill> {
        override val registry = Skill.key

        val POTATO_SKILL = BuildEntry(Identifier("rpg_test", "potato"), Skill(
            icon = ItemStack(Items.POTATO),
            levels = listOf(
                Skill.Level(1),
                Skill.Level(2),
                Skill.Level(3),
            ),
            name = "Potato Skill",
            description = "Unlocks potatoes & other stuff"
        ))

        override fun bootstrap(registerable: Registerable<Skill>) {
            registerable.register(POTATO_SKILL)
        }
    }
}
