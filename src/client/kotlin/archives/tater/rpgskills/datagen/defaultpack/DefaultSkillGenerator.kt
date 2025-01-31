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
    }

    companion object : BuildsRegistry<Skill> {
        override val registry = Skill.key

        val POTATO_SKILL = Entry(Identifier("rpg_test", "potato"), Skill(
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
