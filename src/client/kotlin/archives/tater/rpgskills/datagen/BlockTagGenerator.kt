package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.RPGSkillsTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Blocks
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import java.util.concurrent.CompletableFuture

class BlockTagGenerator(output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>) :
    FabricTagProvider.BlockTagProvider(output, registriesFuture) {
    override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup?) {
        getOrCreateTagBuilder(RPGSkillsTags.NON_SKILL_POINT_DROP).add(
            Blocks.SCULK,
            Blocks.SCULK_VEIN,
            Blocks.SCULK_CATALYST,
            Blocks.SCULK_SENSOR,
            Blocks.SCULK_SHRIEKER,
            Blocks.CALIBRATED_SCULK_SENSOR,
        )
    }
}