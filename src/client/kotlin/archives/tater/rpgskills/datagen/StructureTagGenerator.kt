package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.RPGSkillsTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.StructureTags
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureKeys
import java.util.concurrent.CompletableFuture

class StructureTagGenerator(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricTagProvider<Structure>(output, RegistryKeys.STRUCTURE, registriesFuture) {
    override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup) {
        getOrCreateTagBuilder(RPGSkillsTags.HAS_SKILL_POOL_STRUCTURE).apply {
            add(
                StructureKeys.STRONGHOLD,
                StructureKeys.END_CITY,
                StructureKeys.MANSION,
                StructureKeys.ANCIENT_CITY,
                StructureKeys.FORTRESS,
                StructureKeys.JUNGLE_PYRAMID,
                StructureKeys.DESERT_PYRAMID,
                StructureKeys.BASTION_REMNANT,
                StructureKeys.MONUMENT,
                StructureKeys.PILLAGER_OUTPOST,
                StructureKeys.TRIAL_CHAMBERS,
            )
            forceAddTag(StructureTags.OCEAN_RUIN)
            forceAddTag(StructureTags.SHIPWRECK)
        }
    }
}