package archives.tater.rpgskills.datagen.testpack

import io.wispforest.accessories.api.data.AccessoriesTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.item.Items
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class TestItemTagsGenerator(
    output: FabricDataOutput,
    completableFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricTagProvider.ItemTagProvider(output, completableFuture) {
    override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup?) {
        getOrCreateTagBuilder(AccessoriesTags.CHARM_TAG).add(
            Items.COD,
        )
    }
}
