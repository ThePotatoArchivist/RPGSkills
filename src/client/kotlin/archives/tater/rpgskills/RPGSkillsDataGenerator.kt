package archives.tater.rpgskills

import archives.tater.rpgskills.datagen.LangGenerator
import archives.tater.rpgskills.datagen.ModelGenerator
import archives.tater.rpgskills.datagen.EntityTagGenerator
import archives.tater.rpgskills.datagen.StructureTagGenerator
import archives.tater.rpgskills.datagen.testpack.TestSkillClassGenerator
import archives.tater.rpgskills.datagen.testpack.TestSkillGenerator
import archives.tater.rpgskills.datagen.testpack.TestSkillsLockGenerator
import archives.tater.rpgskills.datagen.testpack.TestItemTagsGenerator
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.registry.RegistryBuilder

object RPGSkillsDataGenerator : DataGeneratorEntrypoint {
	override fun buildRegistry(registryBuilder: RegistryBuilder) {
		TestSkillGenerator.buildRegistry(registryBuilder)
	}

	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		fabricDataGenerator.createPack().apply {
			addProvider(::LangGenerator)
			addProvider(::ModelGenerator)
			addProvider(::StructureTagGenerator)
			addProvider(::EntityTagGenerator)
		}
		fabricDataGenerator.createBuiltinResourcePack(RPGSkills.id("test_pack")).apply {
			addProvider(::TestSkillGenerator)
			addProvider(::TestSkillsLockGenerator)
			addProvider(::TestSkillClassGenerator)
			addProvider(::TestItemTagsGenerator)
		}
	}
}

