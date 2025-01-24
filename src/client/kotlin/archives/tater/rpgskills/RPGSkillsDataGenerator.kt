package archives.tater.rpgskills

import archives.tater.rpgskills.data.Skill
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
import net.minecraft.data.DataOutput.OutputType
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import java.util.function.BiConsumer

object RPGSkillsDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		fabricDataGenerator.createPack().apply {
			addProvider(::SkillGenerator)
		}
	}
}

abstract class SkillProvider(
	dataOutput: FabricDataOutput,
) : FabricCodecDataProvider<Skill>(dataOutput, OutputType.DATA_PACK, "rpgskills/skills", Skill.CODEC) {
	override fun getName(): String = "Skills"
}

class SkillGenerator(dataOutput: FabricDataOutput) : SkillProvider(dataOutput) {
	override fun configure(provider: BiConsumer<Identifier, Skill>) {
		provider.accept(Identifier("rpg_test", "potato"), Skill(
			ItemStack(Items.POTATO),
			listOf(
				Skill.Level(1, unlockItems = listOf(Items.POTATO, Items.POISONOUS_POTATO)),
				Skill.Level(2, unlockItems = listOf(Items.POTATO, Items.POISONOUS_POTATO)),
			)
		))
	}

}
