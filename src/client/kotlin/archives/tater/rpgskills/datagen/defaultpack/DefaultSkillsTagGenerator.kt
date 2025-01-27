package archives.tater.rpgskills.datagen.defaultpack

import archives.tater.rpgskills.data.categoryTagOf
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class DefaultSkillsTagGenerator(
    output: FabricDataOutput,
    completableFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricTagProvider.ItemTagProvider(output, completableFuture) {
    override fun configure(arg: RegistryWrapper.WrapperLookup) {
        getOrCreateTagBuilder(LOCKED_BUTTONS).forceAddTag(ItemTags.BUTTONS)
    }

    companion object {
        val LOCKED_BUTTONS = categoryTagOf(Identifier("rpg_test", "buttons"))
    }
}
