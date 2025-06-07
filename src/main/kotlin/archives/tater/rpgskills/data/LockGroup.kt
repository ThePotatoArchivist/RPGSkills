package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.data.cca.SkillsComponent
import archives.tater.rpgskills.util.*
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeEntry
import net.minecraft.registry.*
import net.minecraft.registry.RegistryWrapper.WrapperLookup
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
    val recipes: LockList<List<Identifier>> = LockList(listOf()),
) {
    constructor(
        requirements: Map<RegistryEntry<Skill>, Int>,
        itemName: String? = null,
        items: LockList<RegistryIngredient.Composite<Item>> = LockList.empty(),
        blocks: LockList<RegistryIngredient.Composite<Block>> = LockList.empty(),
        entities: LockList<RegistryIngredient.Composite<EntityType<*>>> = LockList.empty(),
        recipes: LockList<List<Identifier>> = LockList(listOf()),
    ) : this(listOf(requirements), itemName, items, blocks, entities, recipes)

    private constructor(
        requirements: List<Map<RegistryEntry<Skill>, Int>>,
        itemName: Optional<String>,
        items: LockList<RegistryIngredient.Composite<Item>> = LockList.empty(),
        blocks: LockList<RegistryIngredient.Composite<Block>> = LockList.empty(),
        entities: LockList<RegistryIngredient.Composite<EntityType<*>>> = LockList.empty(),
        recipes: LockList<List<Identifier>> = LockList(listOf()),
    ) : this(requirements, itemName.getOrNull(), items, blocks, entities, recipes)

    fun isSatisfiedBy(levels: Map<RegistryEntry<Skill>, Int>) = requirements.any {
        it.all { (skill, level) ->
            levels.getOrDefault(skill, 0) >= level
        }
    }

    fun isSatisfiedBy(player: PlayerEntity): Boolean = isSatisfiedBy(player[SkillsComponent].skills)

    fun itemNameText() = itemName.let(Text::literal) ?: DEFAULT_ITEM_NAME.text()
    fun itemMessage() = items.message?.let(Text::literal) ?: DEFAULT_ITEM_MESSAGE.text()
    fun blockMessage() = blocks.message?.let(Text::literal) ?: DEFAULT_BLOCK_MESSAGE.text()
    fun entityMessage() = entities.message?.let(Text::literal) ?: DEFAULT_ENTITY_MESSAGE.text()
    fun recipeMessage() = recipes.message?.let(Text::literal) ?: DEFAULT_RECIPE_MESSAGE.text()

    @JvmRecord
    data class LockList<T>(
        val entries: T,
        val message: String? = null,
    ) {
        private constructor(entries: T, message: Optional<String>) : this(entries, message.getOrNull())

        companion object {
            fun <T> createCodec(containerCodec: Codec<T>): Codec<LockList<T>> = RecordCodecBuilder.create { instance -> instance.group(
                field("entries", LockList<T>::entries, containerCodec),
                optionalField("message", LockList<T>::message, Codec.STRING),
            ).apply(instance, ::LockList) }

            fun <T> createCodec(registry: Registry<T>): Codec<LockList<RegistryIngredient.Composite<T>>> = createCodec(RegistryIngredient.createCodec(registry))

            fun <T> empty() = LockList<RegistryIngredient.Composite<T>>(RegistryIngredient.empty())
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
            optionalField("item_name", LockGroup::itemName, Codec.STRING),
            field("items", LockGroup::items, LockList.empty(), LockList.createCodec(Registries.ITEM)),
            field("blocks", LockGroup::blocks, LockList.empty(), LockList.createCodec(Registries.BLOCK)),
            field("entities", LockGroup::entities, LockList.empty(), LockList.createCodec(Registries.ENTITY_TYPE)),
            field("recipes", LockGroup::recipes, LockList(listOf()), LockList.createCodec(Identifier.CODEC.listOf())),
        ).apply(it, ::LockGroup) }

        override val key: RegistryKey<Registry<LockGroup>> = RegistryKey.ofRegistry(RPGSkills.id("lockgroup"))

        inline fun find(registries: WrapperLookup, crossinline condition: (LockGroup) -> Boolean): LockGroup? =
            registries[LockGroup].streamEntries().filter { condition(it.value) }.findFirst().getOrNull()?.value

        @JvmStatic fun find(registries: WrapperLookup, stack: ItemStack) = find(registries) { it.items.entries.test(stack.item) }
        @JvmStatic fun find(registries: WrapperLookup, state: BlockState) = find(registries) { it.blocks.entries.test(state.block) }
        @JvmStatic fun find(registries: WrapperLookup, entity: Entity) = find(registries) { it.entities.entries.test(entity.type) }
        @JvmStatic fun find(registries: WrapperLookup, recipe: RecipeEntry<*>) = find(registries) { recipe.id in it.recipes.entries }

        private fun LockGroup.check(player: PlayerEntity) = takeIf { !isSatisfiedBy(player) }

        @JvmStatic fun findLocked(player: PlayerEntity, stack: ItemStack) = find(player.registryManager) { it.items.entries.test(stack.item) }?.check(player)
        @JvmStatic fun findLocked(player: PlayerEntity, state: BlockState) = find(player.registryManager) { it.blocks.entries.test(state.block) }?.check(player)
        @JvmStatic fun findLocked(player: PlayerEntity, entity: Entity) = find(player.registryManager) { it.entities.entries.test(entity.type) }?.check(player)
        @JvmStatic fun findLocked(player: PlayerEntity, recipe: RecipeEntry<*>) = find(player.registryManager) { recipe.id in it.recipes.entries }?.check(player)

        @JvmStatic fun isLocked(player: PlayerEntity, stack: ItemStack) = findLocked(player, stack) != null
        @JvmStatic fun isLocked(player: PlayerEntity, state: BlockState) = findLocked(player, state) != null
        @JvmStatic fun isLocked(player: PlayerEntity, entity: Entity) = findLocked(player, entity) != null
        @JvmStatic fun isLocked(player: PlayerEntity, recipe: RecipeEntry<*>) = findLocked(player, recipe) != null
    }

}
