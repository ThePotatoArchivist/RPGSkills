package archives.tater.rpgskills.datagen.testpack

import archives.tater.rpgskills.RPGSkillsTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.entity.EntityType
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class TestEntityTagGenerator(
    output: FabricDataOutput,
    completableFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricTagProvider.EntityTypeTagProvider(output, completableFuture) {
    override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup?) {
        with (getOrCreateTagBuilder(RPGSkillsTags.INCREASES_LEVEL_CAP)) {
            add(EntityType.ENDER_DRAGON, EntityType.WITHER)
        }
    }

}