package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.ItemLockTooltip
import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.ceilDiv
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.SpawnEggItem
import net.minecraft.recipe.RecipeManager
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

class LockGroupWidget(x: Int, y: Int, width: Int, lockGroup: LockGroup, registryLookup: WrapperLookup?, recipeManager: RecipeManager?) :
    ClickableWidget(x, y, width, 0, Text.empty()) {

    private val requireText: List<Text> = mutableListOf<Text>().also { ItemLockTooltip.appendRequirements(lockGroup, it, tooltip = false) }
    private val requireTextHeight = textRenderer.fontHeight * requireText.size + GAP

    private val columns = (width - 2 * MARGIN) / SLOT_SIZE

    private val items = lockGroup.items.entries.matchingValues.map { it.defaultStack }
    private val blocks = lockGroup.blocks.entries.matchingValues.map { block -> block.asItem().let {
        if (it == Items.AIR) Items.BARRIER.defaultStack.also { stack -> stack[DataComponentTypes.ITEM_NAME] = block.name } else it.defaultStack
    } }
    private val entities = lockGroup.entities.entries.matchingValues.map { entity ->
        (SpawnEggItem.forEntity(entity) ?: Items.BARRIER).defaultStack.also { stack -> stack[DataComponentTypes.ITEM_NAME] = entity.name }
    }
    private val recipes = if (recipeManager == null || registryLookup == null) listOf() else
        lockGroup.recipes.entries.mapNotNull { recipeManager.get(it).getOrNull()?.value?.getResult(registryLookup) }

    private val groups = mapOf(
        Texts.ITEMS to items,
        Texts.BLOCKS to blocks,
        Texts.ENTITIES to entities,
        Texts.RECIPES to recipes,
    )

    init {
        height = requireTextHeight + getHeight(columns, *groups.values.toTypedArray())
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, width, height)
        requireText.forEachIndexed { index, line ->
            context.drawText(textRenderer, line, x + MARGIN, y + MARGIN + index * textRenderer.fontHeight, 0x404040, false)
        }
        var currentY = MARGIN + y + requireTextHeight
        for ((title, stacks) in groups) {
            if (stacks.isEmpty()) continue
            context.drawText(textRenderer, title.text, x + MARGIN, currentY, 0x404040, false)

            stacks.forEachIndexed { index, stack ->
                val slotX = x + MARGIN + SLOT_SIZE * (index % columns)
                val slotY = currentY + textRenderer.fontHeight + SLOT_SIZE * (index / columns)
                context.drawGuiTexture(SLOT_TEXTURE, slotX, slotY, 0, 18, 18);
                context.drawItem(stack, slotX + 1, slotY + 1)
                context.drawItemInSlot(textRenderer, stack, slotX + 1, slotY + 1)
            }

            currentY += getHeight(columns, stacks)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {}

    companion object {
        private fun getHeight(columns: Int, vararg lists: List<ItemStack>): Int = 2 * MARGIN +
            lists.sumOf { stacks -> getHeight(columns, stacks) } - GAP

        private fun getHeight(columns: Int, stacks: List<ItemStack>) =
            if (stacks.isEmpty()) 0 else textRenderer.fontHeight + GAP + (stacks.size ceilDiv columns) * SLOT_SIZE

        val SLOT_TEXTURE: Identifier = Identifier.ofVanilla("container/slot")

        val BACKGROUND_TEXTURE = RPGSkills.id("border9")

        const val GAP = 4
        const val MARGIN = 6
        const val SLOT_SIZE = 18

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }

    object Texts {
        private fun of(name: String) = Translation.unit("screen.widget.rpgskills.lockgroup.$name")

        val ITEMS = of("items")
        val BLOCKS = of("blocks")
        val ENTITIES = of("entities")
        val RECIPES = of("recipes")
    }
}
