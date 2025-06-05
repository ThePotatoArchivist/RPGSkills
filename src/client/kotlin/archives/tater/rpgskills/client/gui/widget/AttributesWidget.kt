package archives.tater.rpgskills.client.gui.widget

import archives.tater.rpgskills.data.Skill
import archives.tater.rpgskills.util.Translation
import archives.tater.rpgskills.util.value
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.component.type.AttributeModifiersComponent
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import kotlin.math.abs

class AttributesWidget(x: Int, y: Int, width: Int, attributes: Map<RegistryEntry<EntityAttribute>, Skill.AnonymousAttributeModifier>) :
    ClickableWidget(x, y, width, textRenderer.fontHeight * (attributes.size + 1) + 2 * MARGIN, Text.empty()) {

    val text = attributes.mapNotNull { (attribute, modifier) ->
        if (modifier.amount == 0.0) return@mapNotNull null

        val amount = modifier.amount * when (modifier.operation) {
            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL -> 100
            else -> if (attribute == EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) 10 else 1
        }

        Text.translatable(
            "attribute.modifier.${if (amount > 0.0) "plus" else "take"}.${modifier.operation.id}",
            AttributeModifiersComponent.DECIMAL_FORMAT.format(abs(amount)),
            Text.translatable(attribute.value.translationKey)
        ).formatted(attribute.value.getFormatting(amount > 0.0))
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawText(textRenderer, TITLE.text, x + MARGIN, y + MARGIN, 0x404040, false)
        text.forEachIndexed { index, text ->
            context.drawText(textRenderer, text, x + MARGIN, y + MARGIN + (index + 1) * textRenderer.fontHeight, 0x404040, false)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

    }

    companion object {
        val TITLE = Translation.unit("screen.widget.rpgskills.attributes.title")

        const val MARGIN = 2

        private val textRenderer = MinecraftClient.getInstance().textRenderer
    }

}