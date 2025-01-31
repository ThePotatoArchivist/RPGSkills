package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.RegistryKeyHolder
import archives.tater.rpgskills.util.field
import archives.tater.rpgskills.util.get
import archives.tater.rpgskills.util.value
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class LockGroup(
    val items: Ingredient = Ingredient.EMPTY,
    val recipes: List<Identifier> = listOf(),
    val requirements: List<Map<RegistryKey<Skill>, Int>> = listOf(),
    val itemName: String? = null,
    val itemMessage: String? = null,
) {
    fun isSatisfiedBy(player: PlayerEntity): Boolean = requirements.any {
        it.all { (skill, level) ->
            (player[SkillsComponent].levels[skill] ?: 0) >= level
        }
    }

    companion object : RegistryKeyHolder<Registry<LockGroup>> {
        val CODEC: Codec<LockGroup> = RecordCodecBuilder.create {
            it.group(
                field("items", LockGroup::items, Ingredient.EMPTY, INGREDIENT_CODEC),
                field("recipes", LockGroup::recipes, listOf(), Identifier.CODEC.listOf()),
                field("requirements", LockGroup::requirements, listOf(), Codec.unboundedMap(RegistryKey.createCodec(Skill.key), Codec.INT).singleOrList()),
                field("item_name", LockGroup::itemName, null, Codec.STRING),
                field("item_message", LockGroup::itemMessage, null, Codec.STRING),
            ).apply(it, ::LockGroup)
        }

        override val key: RegistryKey<Registry<LockGroup>> = RegistryKey.ofRegistry(RPGSkills.id("lockgroup"))

        private var allLocked: Ingredient? = null

        private fun findLocked(registryManager: DynamicRegistryManager) {
            allLocked = DefaultCustomIngredients.any(*registryManager[this].map { it.items }.toTypedArray())
        }

        fun clearLocked() {
            allLocked = null
        }

        @JvmStatic
        fun of(registryManager: DynamicRegistryManager, stack: ItemStack): RegistryEntry<LockGroup>? =
            registryManager[this].indexedEntries.firstOrNull { it.value.items.test(stack) }

        @JvmStatic
        @Deprecated("Mixin convenience", ReplaceWith("of(player.world.registryManager, stack as ItemStack)", "archives.tater.rpgskills.data.LockGroup.Companion.of", "net.minecraft.item.ItemStack"))
        fun of(player: PlayerEntity, stack: Any) = of(player.world.registryManager, stack as ItemStack)

        @JvmStatic
        fun isLocked(player: PlayerEntity, stack: ItemStack): Boolean {
            if (allLocked == null)
                findLocked(player.world.registryManager)
            return allLocked!!.test(stack) && of(player.world.registryManager, stack)?.value?.isSatisfiedBy(player) == false
        }

        @JvmStatic
        @Deprecated("Mixin convenience", ReplaceWith("isLocked(player, stack as ItemStack)", "archives.tater.rpgskills.data.LockGroup.Companion.isLocked", "net.minecraft.item.ItemStack"))
        fun isLocked(player: PlayerEntity, stack: Any) = isLocked(player, stack as ItemStack)

        @JvmStatic
        @get:JvmName("itemName")
        val RegistryEntry<LockGroup>.itemName: MutableText get() = value.itemName?.let(Text::literal) ?: Text.translatable(key.get().value.toTranslationKey("lockgroup", "name"))
        @JvmStatic
        @get:JvmName("itemMessage")
        val RegistryEntry<LockGroup>.itemMessage: MutableText get() = value.itemMessage?.let(Text::literal) ?: Text.translatable(key.get().value.toTranslationKey("lockgroup", "message"))

        @JvmStatic
        fun nameOf(player: PlayerEntity, stack: ItemStack) = of(player.world.registryManager, stack)?.itemName
        @JvmStatic
        @Deprecated("Mixin convenience", ReplaceWith("nameOf(player, stack as ItemStack) ?: original", "archives.tater.rpgskills.data.LockGroup.Companion.nameOf", "net.minecraft.item.ItemStack"))
        fun nameOf(player: PlayerEntity?, stack: Any, original: Text) = player?.let { nameOf(player, stack as ItemStack) } ?: original

        @JvmStatic
        fun messageOf(player: PlayerEntity, stack: ItemStack) = of(player.world.registryManager, stack)?.itemMessage
        @JvmStatic
        @Deprecated("Mixin convenience", ReplaceWith("Companion.messageOf(player, stack as ItemStack) ?: original", "archives.tater.rpgskills.data.LockGroup.Companion", "net.minecraft.item.ItemStack"))
        fun messageOf(player: PlayerEntity, stack: Any): MutableText = messageOf(player, stack as ItemStack) ?: Text.literal("")
    }
}
