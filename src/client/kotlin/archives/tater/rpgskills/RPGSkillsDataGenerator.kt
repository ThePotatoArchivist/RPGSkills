package archives.tater.rpgskills

import archives.tater.rpgskills.datagen.LanguageGenerator
import archives.tater.rpgskills.datagen.defaultpack.DefaultSkillGenerator
import archives.tater.rpgskills.datagen.defaultpack.DefaultSkillsLanguageGenerator
import archives.tater.rpgskills.datagen.defaultpack.DefaultSkillsTagGenerator
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object RPGSkillsDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		fabricDataGenerator.createBuiltinResourcePack(RPGSkills.id("default_pack")).apply {
			addProvider(::DefaultSkillGenerator)
			addProvider(::DefaultSkillsLanguageGenerator)
			addProvider(::DefaultSkillsTagGenerator)
		}

		fabricDataGenerator.createPack().apply {
			addProvider(::LanguageGenerator)
		}
	}
}

