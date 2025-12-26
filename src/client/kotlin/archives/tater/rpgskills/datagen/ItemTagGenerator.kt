package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.RPGSkillsTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.item.Items
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import java.util.concurrent.CompletableFuture

class ItemTagGenerator(output: FabricDataOutput, completableFuture: CompletableFuture<RegistryWrapper.WrapperLookup>) :
    FabricTagProvider.ItemTagProvider(output, completableFuture) {
    override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup) {
        getOrCreateTagBuilder(RPGSkillsTags.NOT_PLACEABLE).add(
            Items.ENDER_EYE,
        )
    }
}