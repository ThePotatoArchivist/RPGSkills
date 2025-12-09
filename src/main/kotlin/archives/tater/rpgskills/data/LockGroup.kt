package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.cca.SkillsComponent
import archives.tater.rpgskills.util.*
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancement.AdvancementRewards.Builder.recipe
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.command.argument.EntityArgumentType.entity
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeEntry
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryFixedCodec
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*
import kotlin.jvm.optionals.getOrNull

@JvmRecord
data class LockGroup(
    val requirements: List<Map<RegistryEntry<Skill>, Int>>,
    val itemName: String? = null,
    val items: LockList<RegistryIngredient.Composite<Item>> = LockList.empty(),
    val blocks: LockList<RegistryIngredient.Composite<Block>> = LockList.empty(),
    val entities: LockList<RegistryIngredient.Composite<EntityType<*>>> = LockList.empty(),
    val enchantments: LockList<RegistryIngredient.Composite<Enchantment>> = LockList.empty(),
    val recipes: LockList<List<Identifier>> = LockList(listOf()),
) {
    constructor(
        requirements: Map<RegistryEntry<Skill>, Int>,
        itemName: String? = null,
        items: LockList<RegistryIngredient.Composite<Item>> = LockList.empty(),
        blocks: LockList<RegistryIngredient.Composite<Block>> = LockList.empty(),
        entities: LockList<RegistryIngredient.Composite<EntityType<*>>> = LockList.empty(),
        enchantments: LockList<RegistryIngredient.Composite<Enchantment>> = LockList.empty(),
        recipes: LockList<List<Identifier>> = LockList(listOf()),
    ) : this(listOf(requirements), itemName, items, blocks, entities, enchantments, recipes)

    private constructor(
        requirements: List<Map<RegistryEntry<Skill>, Int>>,
        itemName: Optional<String>,
        items: LockList<RegistryIngredient.Composite<Item>> = LockList.empty(),
        blocks: LockList<RegistryIngredient.Composite<Block>> = LockList.empty(),
        entities: LockList<RegistryIngredient.Composite<EntityType<*>>> = LockList.empty(),
        enchantments: LockList<RegistryIngredient.Composite<Enchantment>> = LockList.empty(),
        recipes: LockList<List<Identifier>> = LockList(listOf()),
    ) : this(requirements, itemName.getOrNull(), items, blocks, entities, enchantments, recipes)

    fun isSatisfiedBy(levels: Map<RegistryEntry<Skill>, Int>) = requirements.any {
        it.all { (skill, level) ->
            levels.getOrDefault(skill, 0) >= level
        }
    }

    fun isSatisfiedBy(player: PlayerEntity): Boolean = isSatisfiedBy(player[SkillsComponent].skills)

    fun itemNameText() = itemName?.let(Text::literal) ?: DEFAULT_ITEM_NAME.text()
    fun itemMessage() = items.message?.let(Text::literal) ?: DEFAULT_ITEM_MESSAGE.text()
    fun blockMessage() = blocks.message?.let(Text::literal) ?: DEFAULT_BLOCK_MESSAGE.text()
    fun entityMessage() = entities.message?.let(Text::literal) ?: DEFAULT_ENTITY_MESSAGE.text()
    fun enchantmentMessage() = enchantments.message?.let(Text::literal) ?: DEFAULT_ENCHANTMENT_MESSAGE.text()
    fun recipeMessage() = recipes.message?.let(Text::literal) ?: DEFAULT_RECIPE_MESSAGE.text()

    @JvmRecord
    data class LockList<T>(
        val entries: T,
        val message: String? = null,
    ) {
        private constructor(entries: T, message: Optional<String>) : this(entries, message.getOrNull())

        companion object {
            fun <T> createCodec(containerCodec: Codec<T>): Codec<LockList<T>> = RecordCodecBuilder.create { instance -> instance.group(
                containerCodec.fieldOf("entries").forGetter(LockList<T>::entries),
                Codec.STRING.optionalFieldOf("message").forGetter(LockList<T>::message),
            ).apply(instance, ::LockList) }

            fun <T> createCodec(registry: RegistryKey<Registry<T>>): Codec<LockList<RegistryIngredient.Composite<T>>> =
                createCodec(RegistryIngredient.createCodec(registry))

            fun <T> empty() = LockList<RegistryIngredient.Composite<T>>(RegistryIngredient.empty())
        }
    }

    companion object Manager : RegistryKeyHolder<Registry<LockGroup>> {
        val DEFAULT_ITEM_MESSAGE = Translation.unit("rpgskills.lockgroup.item.message.default")
        val DEFAULT_ITEM_NAME = Translation.unit("rpgskills.lockgroup.item.name.default")
        val DEFAULT_BLOCK_MESSAGE = Translation.unit("rpgskills.lockgroup.block.message.default")
        val DEFAULT_ENTITY_MESSAGE = Translation.unit("rpgskills.lockgroup.entity.message.default")
        val DEFAULT_ENCHANTMENT_MESSAGE = Translation.unit("rpgskills.lockgroup.enchantment.message.default")
        val DEFAULT_RECIPE_MESSAGE = Translation.unit("rpgskills.lockgroup.recipe.message.default")

        val CODEC: Codec<LockGroup> = RecordCodecBuilder.create { it.group(
            Codec.unboundedMap(RegistryFixedCodec.of(Skill.key), Codec.INT).singleOrList().fieldOf("requirements").forGetter(LockGroup::requirements),
            Codec.STRING.optionalFieldOf("item_name").forGetter(LockGroup::itemName),
            LockList.createCodec(RegistryKeys.ITEM).optionalFieldOf("items", LockList.empty()).forGetter(LockGroup::items),
            LockList.createCodec(RegistryKeys.BLOCK).optionalFieldOf("blocks", LockList.empty()).forGetter(LockGroup::blocks),
            LockList.createCodec(RegistryKeys.ENTITY_TYPE).optionalFieldOf("entities", LockList.empty()).forGetter(LockGroup::entities),
            LockList.createCodec(RegistryKeys.ENCHANTMENT).optionalFieldOf("enchantments", LockList.empty()).forGetter(LockGroup::enchantments),
            LockList.createCodec(Identifier.CODEC.listOf()).optionalFieldOf("recipes", LockList(listOf())).forGetter(LockGroup::recipes),
        ).apply(it, ::LockGroup) }

        override val key: RegistryKey<Registry<LockGroup>> = RegistryKey.ofRegistry(RPGSkills.id("lockgroup"))

        private val ITEM_CACHE = RegistryCache(key) { it.value.items.entries.matchingValues }
        private val BLOCK_CACHE = RegistryCache(key) { it.value.blocks.entries.matchingValues }
        private val ENTITY_CACHE = RegistryCache(key) { it.value.entities.entries.matchingValues }
        private val ENCHANTMENT_CACHE = RegistryCache(key) { it.value.enchantments.entries.matchingEntries }
        private val RECIPE_CACHE = RegistryCache(key) { it.value.recipes.entries }

        private fun <T> findLocked(player: PlayerEntity, cache: RegistryCache<T, LockGroup>, value: T) =
            cache[player.registryManager][value]?.value?.takeIf { !it.isSatisfiedBy(player) }

        @JvmStatic fun findLocked(player: PlayerEntity, stack: ItemStack) = findLocked(player, ITEM_CACHE, stack.item)
        @JvmStatic fun findLocked(player: PlayerEntity, state: BlockState) = findLocked(player, BLOCK_CACHE, state.block)
        @JvmStatic fun findLocked(player: PlayerEntity, entity: Entity) = findLocked(player, ENTITY_CACHE, entity.type)
        @JvmStatic fun findLocked(player: PlayerEntity, enchantment: RegistryEntry<Enchantment>) = findLocked(player, ENCHANTMENT_CACHE, enchantment)
        @JvmStatic fun findLocked(player: PlayerEntity, recipe: RecipeEntry<*>) = findLocked(player, RECIPE_CACHE, recipe.id)

        @JvmStatic fun isLocked(player: PlayerEntity, stack: ItemStack) = findLocked(player, stack) != null
        @JvmStatic fun isLocked(player: PlayerEntity, state: BlockState) = findLocked(player, state) != null
        @JvmStatic fun isLocked(player: PlayerEntity, entity: Entity) = findLocked(player, entity) != null
        @JvmStatic fun isLocked(player: PlayerEntity, enchantment: RegistryEntry<Enchantment>) = findLocked(player, enchantment) != null
        @JvmStatic fun isLocked(player: PlayerEntity, recipe: RecipeEntry<*>) = findLocked(player, recipe) != null
    }

}
