package archives.tater.rpgskills.datagen.defaultpack

import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.LockGroupProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import java.util.function.BiConsumer

class DefaultSkillsLockGenerator(dataOutput: FabricDataOutput) : LockGroupProvider(dataOutput) {
    override fun configure(provider: BiConsumer<Identifier, LockGroup>) {
        provider.accept(Identifier("rpg_test", "potato1"), LockGroup(
            items = Ingredient.ofItems(Items.POTATO, Items.POISONOUS_POTATO),
            requirements = listOf(
                mapOf(
                    DefaultSkillGenerator.POTATO_SKILL.key to 1
                )
            ),
            itemName = "Unknown Potato",
            itemMessage = "You don't if this potato is safe to eat"
        ))
    }
}
