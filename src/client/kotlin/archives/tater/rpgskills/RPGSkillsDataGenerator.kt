package archives.tater.rpgskills

import archives.tater.rpgskills.data.Job
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.datagen.BlockTagGenerator
import archives.tater.rpgskills.datagen.EntityTagGenerator
import archives.tater.rpgskills.datagen.LangGenerator
import archives.tater.rpgskills.datagen.ModelGenerator
import archives.tater.rpgskills.datagen.StructureTagGenerator
import archives.tater.rpgskills.datagen.testpack.*
import archives.tater.rpgskills.util.singleTagGenerator
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryWrapper

object RPGSkillsDataGenerator : DataGeneratorEntrypoint {
	override fun buildRegistry(registryBuilder: RegistryBuilder) {
        TestJobGenerator.buildRegistry(registryBuilder)
		TestSkillGenerator.buildRegistry(registryBuilder)
	}

	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		with (fabricDataGenerator.createPack()) {
			addProvider(::LangGenerator)
			addProvider(::ModelGenerator)
			addProvider(::StructureTagGenerator)
			addProvider(::EntityTagGenerator)
            addProvider(::BlockTagGenerator)
		}
		with (fabricDataGenerator.createBuiltinResourcePack(RPGSkills.id("test_pack"))) {
            addProvider(::TestJobGenerator)
			addProvider(::TestSkillGenerator)
			addProvider(::TestSkillsLockGenerator)
			addProvider(::TestSkillClassGenerator)
			addProvider(::TestItemTagsGenerator)
            addProvider(::TestEntityTagGenerator)
            addProvider(singleTagGenerator(RPGSkillsTags.SKILL_ORDER,
                TestSkillGenerator.GRASS_SKILL.key,
                TestSkillGenerator.POTATO_SKILL.key
            ))
            addProvider(singleTagGenerator(RPGSkillsTags.JOB_ORDER,
                TestJobGenerator.GATHER_WHEAT.key,
                *(2..5).map { TestJobGenerator.OTHERS[it].key }.toTypedArray()
            ))
		}
	}
}

