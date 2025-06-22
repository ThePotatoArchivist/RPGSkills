package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.RPGSkillsTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureKeys
import java.util.concurrent.CompletableFuture

class StructureTagGenerator(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricTagProvider<Structure>(output, RegistryKeys.STRUCTURE, registriesFuture) {
    override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup) {
        getOrCreateTagBuilder(RPGSkillsTags.MID_STRUCTURES).add(
            StructureKeys.FORTRESS,
            StructureKeys.END_CITY,
            StructureKeys.BASTION_REMNANT
        )
        getOrCreateTagBuilder(RPGSkillsTags.HARD_STRUCTURES).add(
            StructureKeys.MONUMENT,
            StructureKeys.MANSION,
            StructureKeys.TRIAL_CHAMBERS
        )
    }
}