package archives.tater.rpgskills.client.gui

import archives.tater.rpgskills.util.snakeToTitleCase
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import java.util.WeakHashMap

private val TAGS = WeakHashMap<TagKey<*>, String>()

fun TagKey<*>.fallbackText(): MutableText = Text.literal(TAGS.computeIfAbsent(this) {
    snakeToTitleCase(it.id.path)
})