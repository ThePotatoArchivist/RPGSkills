package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.ItemLockTooltip
import archives.tater.rpgskills.client.util.getMousePosScrolled
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.util.*
import net.minecraft.advancement.criterion.ConsumeItemCriterion.Conditions.item
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.SpawnEggItem
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.util.UseAction
import java.util.stream.Collectors
import kotlin.jvm.optionals.getOrNull

class LockGroupWidget(x: Int, y: Int, width: Int, lockGroup: LockGroup, player: PlayerEntity) :
    ClickableWidget(x, y, width, 0, Text.empty()) {

    private val requireText: List<Text> = mutableListOf<Text>().also { ItemLockTooltip.appendRequirements(lockGroup, player, it, tooltip = false) }
    private val requireTextHeight = textRenderer.fontHeight * requireText.size + GAP

    private val columns = (width - 2 * MARGIN) / SLOT_SIZE

    private val canUse = buildList {
        for (item in lockGroup.items.entries.matchingValues)
            if ((item !is BlockItem && !ENTITY_ITEMS.containsValue(item.defaultStack)) || item.defaultStack.useAction != UseAction.NONE)
                add(item.defaultStack)
        for (block in lockGroup.blocks.entries.matchingValues)
            add(itemOf(block))
        for (entity in lockGroup.entities.entries.matchingValues)
            add(itemOf(entity))
    }
    private val canPlace = lockGroup.items.entries.matchingValues.mapNotNull { (it as? BlockItem)?.defaultStack }
    private val enchantments = lockGroup.enchantments.entries.matchingEntries.map { enchantment ->
        Items.ENCHANTED_BOOK.defaultStack.also { stack -> stack[DataComponentTypes.ITEM_NAME] = enchantment.value.description }
    }
    private val recipes = lockGroup.recipes.entries.matchingValues.map { it.defaultStack }

    private val groups = mapOf(
        Texts.CAN_USE to canUse,
        Texts.CAN_PLACE to canPlace,
        Texts.ENCHANTMENTS to enchantments,
        Texts.RECIPES to recipes,
    )

    init {
        height = requireTextHeight + getHeight(columns, *groups.values.toTypedArray())
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val (tMouseX, tMouseY) = getMousePosScrolled(context, mouseX, mouseY)

        requireText.forEachIndexed { index, line ->
            context.drawText(textRenderer, line, x + MARGIN, y + MARGIN + index * textRenderer.fontHeight, 0x404040, false)
        }
        var currentY = MARGIN + y + requireTextHeight
        var tooltipStack: ItemStack? = null
        for ((title, stacks) in groups) {
            if (stacks.isEmpty()) continue
            context.drawText(textRenderer, title.text, x + MARGIN, currentY, 0x404040, false)

            stacks.forEachIndexed { index, stack ->
                val slotX = x + MARGIN + SLOT_SIZE * (index % columns)
                val slotY = currentY + textRenderer.fontHeight + SLOT_SIZE * (index / columns)
                context.drawGuiTexture(SLOT_TEXTURE, slotX, slotY, 0, 18, 18)
                context.drawItem(stack, slotX + 1, slotY + 1)
                context.drawItemInSlot(textRenderer, stack, slotX + 1, slotY + 1)
                if (context.scissorContains(mouseX, mouseY) && tMouseX in slotX..<(slotX + 18) && tMouseY in slotY..<(slotY + 18)) {
                    tooltipStack = stack
                    HandledScreen.drawSlotHighlight(context, slotX + 1, slotY + 1, 0)
                }
            }

            currentY += getHeight(columns, stacks)
        }
        if (tooltipStack != null) MinecraftClient.getInstance().currentScreen?.run {
            setTooltip(tooltipStack.name)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {}

    companion object {
        private fun getHeight(columns: Int, vararg lists: List<ItemStack>): Int = 2 * MARGIN +
            lists.sumOf { stacks -> getHeight(columns, stacks) } - GAP

        private fun getHeight(columns: Int, stacks: List<ItemStack>) =
            if (stacks.isEmpty()) 0 else textRenderer.fontHeight + GAP + (stacks.size ceilDiv columns) * SLOT_SIZE

        val SLOT_TEXTURE: Identifier = Identifier.ofVanilla("container/slot")

        const val GAP = 4
        const val MARGIN = 6
        const val SLOT_SIZE = 18

        private val textRenderer = MinecraftClient.getInstance().textRenderer

        private val ENTITY_ITEMS by lazy {
            Registries.ENTITY_TYPE.streamEntries().associateToMap {
                it.value to
                        (SpawnEggItem.forEntity(it.value)?.defaultStack
                            ?: Registries.ITEM.getOrEmpty(it.registryKey().value).getOrNull()?.defaultStack
                            ?: Items.BARRIER.defaultStack.apply {
                                set(DataComponentTypes.RARITY, Rarity.COMMON)
                            }
                                ).apply {
                                set(DataComponentTypes.ITEM_NAME, it.value.name)
                            }
            }
        }

        fun itemOf(block: Block): ItemStack = when (val item = block.asItem()) {
            Items.AIR -> Items.BARRIER.defaultStack.also { stack -> stack[DataComponentTypes.ITEM_NAME] = block.name }
            else -> item.defaultStack
        }

        fun itemOf(entity: EntityType<*>): ItemStack = ENTITY_ITEMS[entity]!!
    }

    object Texts {
        private fun of(name: String) = Translation.unit("screen.widget.rpgskills.lockgroup.$name")

        val CAN_USE = of("can_use")
        val CAN_PLACE = of("can_place")
        val ENCHANTMENTS = of("enchantments")
        val RECIPES = of("recipes")
    }
}
