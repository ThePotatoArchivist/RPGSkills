package archives.tater.rpgskills

import archives.tater.rpgskills.datagen.BlockTagGenerator
import archives.tater.rpgskills.datagen.EntityTagGenerator
import archives.tater.rpgskills.datagen.LangGenerator
import archives.tater.rpgskills.datagen.ModelGenerator
import archives.tater.rpgskills.datagen.StructureTagGenerator
import archives.tater.rpgskills.datagen.testpack.*
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.registry.RegistryBuilder

object RPGSkillsDataGenerator : DataGeneratorEntrypoint {
	override fun buildRegistry(registryBuilder: RegistryBuilder) {
        TestJobGenerator.buildRegistry(registryBuilder)
		TestSkillGenerator.buildRegistry(registryBuilder)
	}

	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		fabricDataGenerator.createPack().apply {
			addProvider(::LangGenerator)
			addProvider(::ModelGenerator)
			addProvider(::StructureTagGenerator)
			addProvider(::EntityTagGenerator)
            addProvider(::BlockTagGenerator)
		}
		fabricDataGenerator.createBuiltinResourcePack(RPGSkills.id("test_pack")).apply {
            addProvider(::TestJobGenerator)
			addProvider(::TestSkillGenerator)
			addProvider(::TestSkillsLockGenerator)
			addProvider(::TestSkillClassGenerator)
			addProvider(::TestItemTagsGenerator)
            addProvider(::TestEntityTagGenerator)
		}
	}
}

