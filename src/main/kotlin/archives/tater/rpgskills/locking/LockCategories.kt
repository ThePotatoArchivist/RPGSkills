@file:JvmName("LockCategories")

package archives.tater.rpgskills.locking

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.isIn
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier

private var lockedCategoryTags: List<TagKey<Item>>? = null

const val CATEGORY_TAG_PREFIX = "rpgskills/lockcategory/"

fun categoryTagOf(category: Identifier): TagKey<Item> = TagKey.of(RegistryKeys.ITEM, category.withPrefixedPath(
    CATEGORY_TAG_PREFIX
))

val DEFAULT_LOCK_CATEGORY = categoryTagOf(RPGSkills.id("default"))

fun findCategories() {
    lockedCategoryTags = Registries.ITEM.streamTags()
        .filter { it.id.run { path.startsWith(CATEGORY_TAG_PREFIX) } }
        .toList()
}

fun lockCategoryOf(stack: ItemStack): Identifier {
    if (lockedCategoryTags == null) findCategories()
    return (lockedCategoryTags!!.firstOrNull { stack isIn it } ?: DEFAULT_LOCK_CATEGORY).id.withPath { it.removePrefix(
        CATEGORY_TAG_PREFIX
    ) }
}

fun lockTranslationOf(stack: ItemStack): String {
    return lockCategoryOf(stack).toTranslationKey("rpgskills.lockcategory")
}

fun lockTranslationOf(stack: ItemStack, suffix: String): String {
    return lockCategoryOf(stack).toTranslationKey("rpgskills.lockcategory", suffix)
}

@Deprecated("Only for convenience in mixin",
    ReplaceWith("lockTranslationOf(stack as ItemStack)", "net.minecraft.item.ItemStack")
)
internal fun lockTranslationOf(stack: Any) = lockTranslationOf(stack as ItemStack)
@Deprecated("Only for convenience in mixin",
    ReplaceWith("lockTranslationOf(stack as ItemStack, suffix)", "net.minecraft.item.ItemStack"),
)
internal fun lockTranslationOf(stack: Any, suffix: String) = lockTranslationOf(stack as ItemStack, suffix)

fun lockProcessName(stack: ItemStack, player: PlayerEntity?, original: Text): Text =
    if (stack.hasCustomName() || !isItemLocked(stack, player)) original else Text.translatable(lockTranslationOf(stack, "name"))

@Deprecated("Only for convenience in mixin",
    ReplaceWith("archives.tater.rpgskills.data.lockProcessName(stack as ItemStack, player, original)", "net.minecraft.item.ItemStack")
)
internal fun lockProcessName(stack: Any, player: PlayerEntity?, original: Text) = lockProcessName(stack as ItemStack, player, original)

fun TranslationBuilder.addLockCategory(tag: TagKey<Item>, name: String, message: String) {
    val base = tag.id.withPath { it.removePrefix(CATEGORY_TAG_PREFIX) }.toTranslationKey("rpgskills.lockcategory")
    add("$base.name", name)
    add("$base.message", message)
}

fun clearLockCategories() {
    lockedCategoryTags = null
}
