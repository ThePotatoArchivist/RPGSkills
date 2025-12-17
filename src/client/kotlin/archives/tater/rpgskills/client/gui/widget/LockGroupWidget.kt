package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.ItemLockTooltip
import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.client.util.getMousePosScrolled
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.*
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
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.SpawnEggItem
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.UseAction
import kotlin.jvm.optionals.getOrNull

class LockGroupWidget(x: Int, y: Int, width: Int, lockGroup: LockGroup, skill: RegistryEntry<Skill>, player: PlayerEntity) :
    ClickableWidget(x, y, width, 0, Text.empty()) {

    private val requireText: List<Text> =
        lockGroup.requirementContaining(skill)
            ?.entries
            ?.filter { it.key != skill }
            ?.takeUnless { it.isEmpty() }
            ?.let {
                listOf(
                    ItemLockTooltip.getTitle(lockGroup, player, tooltip = false),
                    ItemLockTooltip.getRequirement(it, player, tooltip = false)
                )
            }
            ?: listOf()
    private val requireTextHeight = if (requireText.isEmpty()) 0 else textRenderer.fontHeight * requireText.size + GAP

    private val columns = (width - 2 * MARGIN) / SLOT_SIZE

    private val canUse = buildList {
        for (item in lockGroup.items.entries.matchingValues)
            if (!item.isPlaced() || item.defaultStack.useAction != UseAction.NONE)
                add(item.defaultStack)
        for (block in lockGroup.blocks.entries.matchingValues)
            add(itemOf(block))
        for (entity in lockGroup.entities.entries.matchingValues)
            add(itemOf(entity))
    }
    private val canPlace = lockGroup.items.entries.matchingValues.mapNotNull { if (it.isPlaced()) it.defaultStack else null }
    private val enchantments = lockGroup.enchantments.entries.matchingEntries.map { enchantment ->
        Items.ENCHANTED_BOOK.defaultStack.also { stack -> stack[DataComponentTypes.ITEM_NAME] = enchantment.value.description }
    }
    private val recipes = lockGroup.recipes.entries.matchingValues.map { it.defaultStack }

    private val groups = listOf(
        Section(Texts.CAN_USE, SLOT_USE_TEXTURE, canUse),
        Section(Texts.CAN_PLACE, SLOT_PLACE_TEXTURE, canPlace),
        Section(Texts.ENCHANTMENTS, SLOT_ENCHANT_TEXTURE, enchantments),
        Section(Texts.RECIPES, SLOT_CRAFT_TEXTURE, recipes),
    )

    init {
        height = requireTextHeight + getTotalHeight(columns, groups.map { it.stacks })
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val (tMouseX, tMouseY) = getMousePosScrolled(context, mouseX, mouseY)

        requireText.forEachIndexed { index, line ->
            context.drawText(textRenderer, line, x + MARGIN, y + MARGIN + index * textRenderer.fontHeight, 0x404040, false)
        }
        var currentY = MARGIN + y + requireTextHeight
        var tooltipStack: ItemStack? = null
        for ((title, slotTexture, stacks) in groups) {
            if (stacks.isEmpty()) continue
            context.drawText(textRenderer, title.text, x + MARGIN, currentY, 0x404040, false)

            stacks.forEachIndexed { index, stack ->
                val slotX = x + MARGIN + SLOT_SIZE * (index % columns)
                val slotY = currentY + textRenderer.fontHeight + SLOT_SIZE * (index / columns)
                context.drawGuiTexture(slotTexture, slotX, slotY, 0, 18, 18)
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

    @JvmRecord
    private data class Section(val title: UnitTranslation, val slotTexture: Identifier, val stacks: List<ItemStack>)

    companion object {
        private fun getTotalHeight(columns: Int, lists: List<List<ItemStack>>): Int = 2 * MARGIN +
            lists.sumOf { stacks -> getHeight(columns, stacks) } - GAP

        private fun getHeight(columns: Int, stacks: List<ItemStack>) =
            if (stacks.isEmpty()) 0 else textRenderer.fontHeight + GAP + (stacks.size ceilDiv columns) * SLOT_SIZE

        val SLOT_USE_TEXTURE = RPGSkills.id("skill/slot_use")
        val SLOT_PLACE_TEXTURE = RPGSkills.id("skill/slot_place")
        val SLOT_ENCHANT_TEXTURE = RPGSkills.id("skill/slot_enchant")
        val SLOT_CRAFT_TEXTURE = RPGSkills.id("skill/slot_craft")

        const val GAP = 4
        const val MARGIN = 6
        const val SLOT_SIZE = 18

        private val textRenderer = MinecraftClient.getInstance().textRenderer

        private val ENTITY_ITEMS by lazy<Map<EntityType<*>, Item>> {
            Registries.ENTITY_TYPE.streamEntries().associateNotNullToMap {
                it.value to
                (SpawnEggItem.forEntity(it.value)
                    ?: Registries.ITEM.getOrEmpty(it.registryKey().value).getOrNull())
            }
        }

        fun Item.isPlaced() = this is BlockItem || ENTITY_ITEMS.containsValue(this)

        fun itemOf(block: Block): ItemStack = (block.asItem().takeUnless { it == Items.AIR } ?: Items.BARRIER).defaultStack

        fun itemOf(entity: EntityType<*>): ItemStack = (ENTITY_ITEMS[entity] ?: Items.BARRIER).defaultStack
    }

    object Texts {
        private fun of(name: String) = Translation.unit("screen.widget.rpgskills.lockgroup.$name")

        val CAN_USE = of("can_use")
        val CAN_PLACE = of("can_place")
        val ENCHANTMENTS = of("enchantments")
        val RECIPES = of("recipes")
    }
}
