package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.RequirementTooltip
import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.RPGSkillsClient
import archives.tater.rpgskills.client.gui.fallbackText
import archives.tater.rpgskills.client.util.getMousePosScrolled
import archives.tater.rpgskills.data.LockGroup
import archives.tater.rpgskills.data.RegistryIngredient
import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.*
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.Items.ENCHANTED_BOOK
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.UseAction

class LockGroupWidget(x: Int, y: Int, width: Int, lockGroup: LockGroup, skill: RegistryEntry<Skill>, level: Int, player: PlayerEntity) :
    ClickableWidget(x, y, width, 0, Text.empty()) {

    private val additionalRequirements = lockGroup.requirementsContaining(skill, level)
        .mapIndexedNotNull { index, requirement ->
            requirement.entries
                .filter { it.key != skill }
                .takeUnless { it.isEmpty() }
                ?.let {
                    RequirementTooltip.getRequirement(it, player, tooltip = false).apply {
                        withColor(getColor(index))
                    }
                }
        }
        .takeUnless { it.isEmpty() }
        ?.joinToText(WidgetTexts.OR.text)

    private val requireTooltip = buildList {
        RequirementTooltip.appendRequirements(lockGroup, player, this)
    }

    private val columns = (width - 2 * MARGIN) / SLOT_SIZE

    private val groups = listOf(
        Section(
            WidgetTexts.CAN_USE.getText(additionalRequirements),
            SLOT_USE_TEXTURE,
            buildList<DisplayedSlot> {
                addAll(lockGroup.items.toDisplayedSlot({ it.name }) { if (LockGroup.isUsedItem(it)) it.defaultStack else null })
                addAll(lockGroup.blocks.toDisplayedSlot({ it.name }) { itemOf(it) })
                addAll(lockGroup.entities.toDisplayedSlot({ it.name }) { itemOf(it) })
            }
        ),
        Section(
            WidgetTexts.CAN_PLACE.getText(additionalRequirements),
            SLOT_PLACE_TEXTURE,
            lockGroup.items.toDisplayedSlot({ it.name }) { if (LockGroup.isPlacedItem(it)) it.defaultStack else null }
        ),
        Section(
            WidgetTexts.CAN_ENCHANT.getText(additionalRequirements),
            SLOT_ENCHANT_TEXTURE,
            lockGroup.enchantments.toDisplayedSlot({ it.description }) {
                ENCHANTED_BOOK.defaultStack
            }
        ),
        Section(
            WidgetTexts.CAN_CRAFT.getText(additionalRequirements),
            SLOT_CRAFT_TEXTURE,
            lockGroup.recipes.toDisplayedSlot({ it.name }) { it.defaultStack }
        ),
    )

    init {
        height = getTotalHeight(columns, groups.map { it.slots })
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val (tMouseX, tMouseY) = getMousePosScrolled(context, mouseX, mouseY)
        val animationCounter = RPGSkillsClient.animationCounter / ANIMATION_RATE

        var currentY = MARGIN + y
        var tooltip: Text? = null
        for ((title, slotTexture, stacks) in groups) {
            if (stacks.isEmpty()) continue
            context.drawText(textRenderer, title, x + MARGIN, currentY, 0x404040, false)

            stacks.forEachIndexed { index, (text, stacks) ->
                val slotX = x + MARGIN + SLOT_SIZE * (index % columns)
                val slotY = currentY + textRenderer.fontHeight + SLOT_SIZE * (index / columns)
                val stack = stacks[animationCounter % stacks.size]
                context.drawGuiTexture(slotTexture, slotX, slotY, 0, 18, 18)
                context.drawItem(stack, slotX + 1, slotY + 1)
                context.drawItemInSlot(textRenderer, stack, slotX + 1, slotY + 1)
                if (context.scissorContains(mouseX, mouseY) && tMouseX in slotX..<(slotX + 18) && tMouseY in slotY..<(slotY + 18)) {
                    tooltip = text
                    HandledScreen.drawSlotHighlight(context, slotX + 1, slotY + 1, 0)
                }
            }

            currentY += getHeight(columns, stacks)
        }
        if (tooltip != null) MinecraftClient.getInstance().currentScreen?.run {
            setTooltip(buildList {
                add(tooltip.asOrderedText())
                addAll(requireTooltip.map { it.asOrderedText() })
            })
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {}

    @JvmRecord
    private data class DisplayedSlot(val text: Text, val stacks: List<ItemStack>)
    @JvmRecord
    private data class Section(val title: Text, val slotTexture: Identifier, val slots: List<DisplayedSlot>)

    companion object {
        private fun getTotalHeight(columns: Int, lists: List<List<*>>): Int = 2 * MARGIN +
            lists.sumOf { stacks -> getHeight(columns, stacks) } - GAP

        private fun getHeight(columns: Int, stacks: List<*>) =
            if (stacks.isEmpty()) 0 else textRenderer.fontHeight + GAP + (stacks.size ceilDiv columns) * SLOT_SIZE

        val SLOT_USE_TEXTURE = RPGSkills.id("skill/slot_use")
        val SLOT_PLACE_TEXTURE = RPGSkills.id("skill/slot_place")
        val SLOT_ENCHANT_TEXTURE = RPGSkills.id("skill/slot_enchant")
        val SLOT_CRAFT_TEXTURE = RPGSkills.id("skill/slot_craft")

        const val GAP = 4
        const val MARGIN = 6
        const val SLOT_SIZE = 18
        const val ANIMATION_RATE = 20

        private val textRenderer = MinecraftClient.getInstance().textRenderer

        fun getColor(index: Int) = when(index) {
            0 -> 0x5F320F // Orange
            1 -> 0x146306 // Green
            2 -> 0x07295C // Blue
            else -> 0xffffff
        }

        fun itemOf(block: Block): ItemStack = (block.asItem().takeUnless { it == Items.AIR } ?: Items.BARRIER).defaultStack

        fun itemOf(entity: EntityType<*>): ItemStack = (LockGroup.ENTITY_ITEMS[entity] ?: Items.BARRIER).defaultStack

        private fun <T> LockGroup.LockList<RegistryIngredient.Composite<T>>.toDisplayedSlot(text: (T) -> Text, transform: (T) -> ItemStack?): List<DisplayedSlot> =
            entries.entries.mapNotNull { entry ->
                entry.matchingValues
                    .mapNotNull(transform)
                    .takeUnless { it.isEmpty() }
                    ?.let { DisplayedSlot(when (entry) {
                        is RegistryIngredient.DirectEntry -> text(entry.entry.value)
                        is RegistryIngredient.TagEntry ->
                            Text.translatable(entry.tag.translationKey).takeIfTranslated()
                                ?: Text.translatable(TagKey.of(RegistryKeys.ITEM, entry.tag.id).translationKey).takeIfTranslated()
                                ?: entry.tag.fallbackText()
                    }, it) }
            }
    }

    object WidgetTexts {
        private fun unit(name: String) = Translation.unit("screen.widget.rpgskills.lockgroup.$name")
        private fun arg(name: String) = Translation.arg("screen.widget.rpgskills.lockgroup.$name")

        private fun of(name: String) = SectionTitle(unit(name), arg("$name.additional"))

        val CAN_USE = of("can_use")
        val CAN_PLACE = of("can_place")
        val CAN_ENCHANT = of("can_enchant")
        val CAN_CRAFT = of("can_craft")
        val OR = unit("or")

        data class SectionTitle(val normal: UnitTranslation, val additional: ArgTranslation) {
            fun getText(additional: Text?) =
                if (additional == null) normal.text else this.additional.text(additional)
        }
    }
}
