package archives.tater.rpgskills

import archives.tater.rpgskills.datagen.DefaultSkillGenerator
import archives.tater.rpgskills.datagen.DefaultSkillsLanguageGenerator
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object RPGSkillsDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		fabricDataGenerator.createBuiltinResourcePack(RPGSkills.id("default_pack")).apply {
			addProvider(::DefaultSkillGenerator)
			addProvider(::DefaultSkillsLanguageGenerator)
		}
	}
}

