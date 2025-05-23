package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.*
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeEntry
import net.minecraft.registry.*
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.registry.entry.RegistryFixedCodec
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*
import kotlin.jvm.optionals.getOrNull

@JvmRecord
data class LockGroup(
    val requirements: List<Map<RegistryEntry<Skill>, Int>>,
    val attributes: Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> = mapOf(),
    val itemName: String?,
    val items: LockList<Ingredient> = LockList(Ingredient.EMPTY),
    val blocks: LockList<RegistryEntryList<Block>> = LockList.empty(),
    val entities: LockList<RegistryEntryList<EntityType<*>>> = LockList.empty(),
    val recipes: LockList<List<Identifier>> = LockList(listOf()),
) {
    constructor(
        requirements: Map<RegistryEntry<Skill>, Int>,
        attributes: Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> = mapOf(),
        itemName: String?,
        items: LockList<Ingredient> = LockList(Ingredient.EMPTY),
        blocks: LockList<RegistryEntryList<Block>> = LockList.empty(),
        entities: LockList<RegistryEntryList<EntityType<*>>> = LockList.empty(),
        recipes: LockList<List<Identifier>> = LockList(listOf()),
    ) : this(listOf(requirements), attributes, itemName, items, blocks, entities, recipes)

    constructor(
        requirements: List<Map<RegistryEntry<Skill>, Int>>,
        attributes: Map<RegistryEntry<EntityAttribute>, EntityAttributeModifier> = mapOf(),
        itemName: Optional<String>,
        items: LockList<Ingredient> = LockList(Ingredient.EMPTY),
        blocks: LockList<RegistryEntryList<Block>> = LockList.empty(),
        entities: LockList<RegistryEntryList<EntityType<*>>> = LockList.empty(),
        recipes: LockList<List<Identifier>> = LockList(listOf()),
    ) : this(requirements, attributes, itemName.getOrNull(), items, blocks, entities, recipes)

    fun isSatisfiedBy(levels: Map<RegistryEntry<Skill>, Int>) = requirements.any {
        it.all { (skill, level) ->
            levels.getOrDefault(skill, 0) >= level
        }
    }

    fun isSatisfiedBy(player: PlayerEntity): Boolean = isSatisfiedBy(player[SkillsComponent].levels)

    fun itemNameText() = itemName.let(Text::literal) ?: DEFAULT_ITEM_NAME.text()
    fun itemMessage() = items.message?.let(Text::literal) ?: DEFAULT_ITEM_MESSAGE.text()
    fun blockMessage() = blocks.message?.let(Text::literal) ?: DEFAULT_BLOCK_MESSAGE.text()
    fun entityMessage() = entities.message?.let(Text::literal) ?: DEFAULT_ENTITY_MESSAGE.text()
    fun recipeMessage() = recipes.message?.let(Text::literal) ?: DEFAULT_RECIPE_MESSAGE.text()

    @JvmRecord
    data class LockList<T>(
        val entries: T,
        val message: String? = null, // TODO this should be nullable in codecs
    ) {
        constructor(entries: T, message: Optional<String>) : this(entries, message.getOrNull())

        companion object {
            fun <T> createCodec(containerCodec: Codec<T>): Codec<LockList<T>> = RecordCodecBuilder.create { instance -> instance.group(
                field("entries", LockList<T>::entries, containerCodec),
                optionalField("message", LockList<T>::message, Codec.STRING),
            ).apply(instance, ::LockList) }

            fun <T> createCodec(registryRef: RegistryKey<Registry<T>>): Codec<LockList<RegistryEntryList<T>>> = createCodec(RegistryCodecs.entryList(registryRef))

            fun <T> empty() = LockList<RegistryEntryList<T>>(RegistryEntryList.empty())
        }
    }

    companion object Manager : RegistryKeyHolder<Registry<LockGroup>> {
        val DEFAULT_ITEM_MESSAGE = Translation.unit("rpgskills.lockgroup.item.message.default")
        val DEFAULT_ITEM_NAME = Translation.unit("rpgskills.lockgroup.item.name.default")
        val DEFAULT_BLOCK_MESSAGE = Translation.unit("rpgskills.lockgroup.block.message.default")
        val DEFAULT_ENTITY_MESSAGE = Translation.unit("rpgskills.lockgroup.entity.message.default")
        val DEFAULT_RECIPE_MESSAGE = Translation.unit("rpgskills.lockgroup.recipe.message.default")


        val CODEC = RecordCodecBuilder.create { it.group(
            field("requirements", LockGroup::requirements, Codec.unboundedMap(RegistryFixedCodec.of(Skill.key), Codec.INT).singleOrList()),
            field("attributes", LockGroup::attributes, mapOf(), Codec.simpleMap(Registries.ATTRIBUTE.entryCodec, EntityAttributeModifier.CODEC, Registries.ATTRIBUTE).codec()),
            optionalField("item_name", LockGroup::itemName, Codec.STRING),
            field("items", LockGroup::items, LockList(Ingredient.EMPTY), LockList.createCodec(Ingredient.DISALLOW_EMPTY_CODEC)),
            field("blocks", LockGroup::blocks, LockList.empty(), LockList.createCodec(RegistryKeys.BLOCK)),
            field("entities", LockGroup::entities, LockList.empty(), LockList.createCodec(RegistryKeys.ENTITY_TYPE)),
            field("recipes", LockGroup::recipes, LockList(listOf()), LockList.createCodec(Identifier.CODEC.listOf())),
        ).apply(it, ::LockGroup) }

        override val key: RegistryKey<Registry<LockGroup>> = RegistryKey.ofRegistry(RPGSkills.id("lockgroup"))

        inline fun find(registries: WrapperLookup, crossinline condition: (LockGroup) -> Boolean): LockGroup? =
            registries[LockGroup].streamEntries().filter { condition(it.value) }.findFirst().getOrNull()?.value

        @JvmStatic fun find(registries: WrapperLookup, stack: ItemStack) = find(registries) { it.items.entries.test(stack) }
        @JvmStatic fun find(registries: WrapperLookup, state: BlockState) = find(registries) { state.isIn(it.blocks.entries) }
        @JvmStatic fun find(registries: WrapperLookup, entity: Entity) = find(registries) { entity.type.isIn(it.entities.entries) }
        @JvmStatic fun find(registries: WrapperLookup, recipe: RecipeEntry<*>) = find(registries) { recipe.id in it.recipes.entries }

        private fun LockGroup.check(player: PlayerEntity) = takeIf { !isSatisfiedBy(player) }

        @JvmStatic fun findLocked(player: PlayerEntity, stack: ItemStack) = find(player.registryManager) { it.items.entries.test(stack) }?.check(player)
        @JvmStatic fun findLocked(player: PlayerEntity, state: BlockState) = find(player.registryManager) { state.isIn(it.blocks.entries) }?.check(player)
        @JvmStatic fun findLocked(player: PlayerEntity, entity: Entity) = find(player.registryManager) { entity.type.isIn(it.entities.entries) }?.check(player)
        @JvmStatic fun findLocked(player: PlayerEntity, recipe: RecipeEntry<*>) = find(player.registryManager) { recipe.id in it.recipes.entries }?.check(player)

        @JvmStatic fun isLocked(player: PlayerEntity, stack: ItemStack) = findLocked(player, stack) != null
        @JvmStatic fun isLocked(player: PlayerEntity, state: BlockState) = findLocked(player, state) != null
        @JvmStatic fun isLocked(player: PlayerEntity, entity: Entity) = findLocked(player, entity) != null
        @JvmStatic fun isLocked(player: PlayerEntity, recipe: RecipeEntry<*>) = findLocked(player, recipe) != null
    }

}