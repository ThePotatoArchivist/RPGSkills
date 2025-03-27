package archives.tater.rpgskills

import archives.tater.rpgskills.datagen.defaultpack.DefaultSkillGenerator
import archives.tater.rpgskills.datagen.defaultpack.DefaultSkillsLockGenerator
import archives.tater.rpgskills.datagen.LangGenerator
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.registry.RegistryBuilder

object RPGSkillsDataGenerator : DataGeneratorEntrypoint {
	override fun buildRegistry(registryBuilder: RegistryBuilder) {
		DefaultSkillGenerator.buildRegistry(registryBuilder)
	}

	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		fabricDataGenerator.createPack().apply {
			addProvider(::LangGenerator)
		}
		fabricDataGenerator.createBuiltinResourcePack(RPGSkills.id("default_pack")).apply {
			addProvider(::DefaultSkillGenerator)
			addProvider(::DefaultSkillsLockGenerator)
		}
	}
}

