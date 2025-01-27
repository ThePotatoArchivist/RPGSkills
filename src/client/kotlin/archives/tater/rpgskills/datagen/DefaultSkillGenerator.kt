package archives.tater.rpgskills.datagen

import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.data.SkillProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import java.util.function.BiConsumer

class DefaultSkillGenerator(dataOutput: FabricDataOutput) : SkillProvider(dataOutput) {
	override fun configure(provider: BiConsumer<Identifier, Skill>) {
		provider.accept(
            Identifier("rpg_test", "potato"), Skill(
                ItemStack(Items.POTATO),
                listOf(
                    Skill.Level(1, unlockItems = listOf(Items.POTATO, Items.POISONOUS_POTATO)),
                    Skill.Level(2, unlockItems = listOf(Items.TRIDENT)),
                    Skill.Level(3, unlockTags = listOf(ItemTags.BUTTONS))
                )
            )
        )
	}
}
