package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.data.ClassProvider
import archives.tater.rpgskills.data.SkillClass
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.item.Items
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class TestSkillClassGenerator(
    dataOutput: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : ClassProvider(dataOutput, registriesFuture) {
    override fun configure(provider: BiConsumer<Identifier, SkillClass>, lookup: RegistryWrapper.WrapperLookup) {
        provider.accept(Identifier.of("rpg_test", "archer"), SkillClass(
            name = "Archer",
            icon = Items.BOW,
            description = "Shoots arrows",
            startingLevels = mapOf(
                TestSkillGenerator.COW_SKILL.entry to 1,
                TestSkillGenerator.GRASS_SKILL.entry to 2
            )
        ))
        provider.accept(Identifier.of("rpg_test", "blacksmith"), SkillClass(
            name = "Blacksmith",
            icon = Items.IRON_AXE,
            description = "Makes stuff",
            startingLevels = mapOf(
                TestSkillGenerator.POTATO_SKILL.entry to 2,
                TestSkillGenerator.GRASS_SKILL.entry to 1
            )
        ))
    }
}