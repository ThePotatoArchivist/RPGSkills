package archives.tater.rpgskills.util

import archives.tater.rpgskills.RPGSkills
import com.google.common.collect.HashMultimap
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.advancement.criterion.BredAnimalsCriterion
import net.minecraft.advancement.criterion.ItemCriterion
import net.minecraft.advancement.criterion.OnKilledCriterion
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.TrackedData
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.predicate.entity.DamageSourcePredicate
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.registry.*
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import org.joml.Vector2i
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import com.mojang.datafixers.util.Pair as DFPair

internal inline fun <T: Any, C> MapCodec<Optional<T>>.forGetter(crossinline getter: (C) -> T?): RecordCodecBuilder<C, Optional<T>> =
    forGetter { Optional.ofNullable(getter(it)) }

inline val <T> RegistryEntry<T>.value: T get() = value()

fun IdentifiableResourceReloadListener(
    fabricId: Identifier, reload: (
        synchronizer: ResourceReloader.Synchronizer,
        manager: ResourceManager,
        prepareProfiler: Profiler,
        applyProfiler: Profiler,
        prepareExecutor: Executor,
        applyExecutor: Executor
    ) -> CompletableFuture<Void>
) = object : IdentifiableResourceReloadListener {
    override fun reload(
        synchronizer: ResourceReloader.Synchronizer,
        manager: ResourceManager,
        prepareProfiler: Profiler,
        applyProfiler: Profiler,
        prepareExecutor: Executor,
        applyExecutor: Executor
    ): CompletableFuture<Void> =
        reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor)

    override fun getFabricId(): Identifier = fabricId
}

fun SimpleSynchronousResourceReloadListener(fabricId: Identifier, reload: (ResourceManager) -> Unit) =
    object : SimpleSynchronousResourceReloadListener {
        override fun reload(manager: ResourceManager) {
            reload(manager)
        }

        override fun getFabricId(): Identifier = fabricId
    }

interface RegistryKeyHolder<T> {
    val key: RegistryKey<T>
}

operator fun <E> RegistryWrapper.WrapperLookup.get(holder: RegistryKeyHolder<out Registry<out E>?>): RegistryWrapper<E> =
    this.getWrapperOrThrow(holder.key)

@Suppress("UnstableApiUsage")
interface AttachmentTypeHolder<T> {
    val attachmentType: AttachmentType<T>
}

@Suppress("UnstableApiUsage")
operator fun <T> AttachmentTarget.get(attachmentType: AttachmentType<T>): T = getAttachedOrCreate(attachmentType)

@Suppress("UnstableApiUsage")
operator fun <T> AttachmentTarget.get(holder: AttachmentTypeHolder<T>): T = this[holder.attachmentType]

interface ComponentKeyHolder<C : Component, in T> {
    val key: ComponentKey<C>
}

operator fun <C : Component, T : Any> T.get(keyHolder: ComponentKeyHolder<C, T>): C = keyHolder.key.get(this)

operator fun <C: Component> ComponentKey<C>.getValue(thisRef: Any, property: KProperty<*>): C = get(thisRef)
operator fun <C: Component> ComponentKeyHolder<C, *>.provideDelegate(thisRef: Any, property: KProperty<*>) = key

operator fun <T> DFPair<T, *>.component1(): T = first
operator fun <T> DFPair<*, T>.component2(): T = second

infix fun ItemStack.isOf(item: Item) = this.isOf(item)
infix fun ItemStack.isIn(tag: TagKey<Item>) = this.isIn(tag)
infix fun EntityType<*>.isIn(tag: TagKey<EntityType<*>>) = this.isIn(tag)
infix fun Entity.isIn(tag: TagKey<EntityType<*>>) = type isIn tag
infix fun <T> RegistryEntry<T>.isIn(tag: TagKey<T>) = this.isIn(tag)

fun <T, K, V> Iterable<T>.associateNotNull(transform: (T) -> Pair<K, V>?) = mapNotNull(transform).toMap()

fun <K, V> hashMultiMapOf(vararg pairs: Pair<K, V>): HashMultimap<K, V> = HashMultimap.create<K, V>().apply {
    pairs.forEach { (key, value) ->
        put(key, value)
    }
}

fun <T> KMutableProperty0<T>.synced(key: ComponentKey<*>, provider: Any) = object : ReadWriteProperty<Component, T> {
    override fun getValue(thisRef: Component, property: KProperty<*>): T = this@synced.get()

    override fun setValue(thisRef: Component, property: KProperty<*>, value: T) {
        this@synced.set(value)
        key.sync(provider)
    }
}

fun <T> Iterable<T>.mapToNbt(transform: (T) -> NbtCompound) = NbtList().apply {
    for (element in this@mapToNbt)
        add(transform(element))
}

fun <T: DataResult<*>> T.logIfError(logger: Logger = RPGSkills.logger): T {
    ifError {
        logger.error("Serialization Error: {}", it.message())
    }
    return this
}

infix fun Int.ceilDiv(other: Int) = (this + other - 1) / other

fun <T> Collection<T>.withFirst(element: T): List<T> {
    val result = ArrayList<T>(size + 1)
    result.add(element)
    result.addAll(this)
    return result
}

operator fun <T> TrackedData<T>.getValue(thisRef: Entity, property: KProperty<*>): T = thisRef.dataTracker[this]
operator fun <T> TrackedData<T>.setValue(thisRef: Entity, property: KProperty<*>, value: T) {
    thisRef.dataTracker[this] = value
}

fun <T> FabricTagProvider<T>.FabricTagBuilder.addOptional(vararg ids: Identifier) {
    for (id in ids)
        addOptional(id)
}

fun RegistryWrapper<*>.isEmpty() = streamEntries().findAny().isEmpty

fun intRangeCodec(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Codec<Int> = Codec.intRange(min, max)

fun itemCriterionConditions(player: LootContextPredicate? = null, location: LootContextPredicate? = null) =
    ItemCriterion.Conditions(Optional.ofNullable(player), Optional.ofNullable(location))

fun onKilledCriterionConditions(
    player: LootContextPredicate? = null,
    entity: LootContextPredicate? = null,
    killingBlow: DamageSourcePredicate? = null
) = OnKilledCriterion.Conditions(
    Optional.ofNullable(player),
    Optional.ofNullable(entity),
    Optional.ofNullable(killingBlow),
)

fun bredAnimalsCriterionConditions(
    player: LootContextPredicate? = null,
    parent: LootContextPredicate? = null,
    partner: LootContextPredicate? = null,
    child: LootContextPredicate? = null
) = BredAnimalsCriterion.Conditions(
    Optional.ofNullable(player),
    Optional.ofNullable(parent),
    Optional.ofNullable(partner),
    Optional.ofNullable(child),
)

val SHORT_STACK_CODEC = AlternateCodec(
    ItemStack.UNCOUNTED_CODEC,
    Registries.ITEM.codec.xmap({ it.defaultStack }, { it.item })
) { ItemStack.areItemsAndComponentsEqual(it, it.item.defaultStack) }

val ONE_INDEX_CODEC: Codec<Int> = Codec.STRING.comapFlatMap(
    {
        val value = it.toIntOrNull() ?: return@comapFlatMap DataResult.error { "Not a valid int: $it" }
        if (value <= 0) return@comapFlatMap DataResult.error { "Value was less than 1: $it" }
        DataResult.success(value)
    },
    { it.toString() }
)

fun <T> Codec<T>.indexedOf(default: T): Codec<List<T>> = Codec.unboundedMap(ONE_INDEX_CODEC, this).xmap(
    { map -> List(map.keys.max()) { map[it + 1] ?: default } },
    { list -> list.withIndex().associateNotNull { (index, entry) -> if (entry != default || index + 1 >= list.size) index + 1 to entry else null } }
)

fun <T> RegistryWrapper<T>.streamEntriesOrdered(tag: TagKey<T>): Stream<out RegistryEntry<T>> = getOptional(tag).getOrNull()?.let { list ->
    Stream.concat(
        list.stream(),
        streamEntries().filter { !(it isIn tag) }
    )
} ?: streamEntries()

fun EntityPredicate(init: EntityPredicate.Builder.() -> Unit): EntityPredicate = EntityPredicate.Builder.create().apply(init).build()

fun EntityPredicate.toLootContextPredicate(): LootContextPredicate = EntityPredicate.asLootContextPredicate(this)

fun <T> singleTagGenerator(tag: TagKey<T>, vararg entries: RegistryKey<T>) =
    FabricDataGenerator.Pack.RegistryDependentFactory<FabricTagProvider<T>> { output, registriesFuture ->
        object : FabricTagProvider<T>(output, tag.registry, registriesFuture) {
            override fun configure(wrapperLookup: RegistryWrapper.WrapperLookup?) {
                getOrCreateTagBuilder(tag).add(*entries)
            }
        }
    }

fun <T> RegistryEntryList(registry: Registry<T>, vararg entries: T): RegistryEntryList.Direct<T> =
    RegistryEntryList.of(entries.map { registry.getEntry(it) })

fun <T> Iterable<T>.joinToText(separator: String = ", ", transform: (T) -> Text): MutableText = Text.empty().apply {
    var first = true
    for (value in this@joinToText) {
        if (!first) append(separator)
        append(transform(value))
        first = false
    }
}

inline fun <T> MutableIterator<T>.removeIf(condition: (T) -> Boolean) {
    for (value in this)
        if (condition(value))
            remove()
}

inline fun <K, V> MutableMap<K, V>.removeIf(condition: (Map.Entry<K, V>) -> Boolean) {
    iterator().removeIf(condition)
}

operator fun Vector2i.component1() = x
operator fun Vector2i.component2() = y

class RegistryAwareXmapCodec<A, B, E>(
    private val registry: RegistryKey<Registry<E>>,
    private val codec: Codec<A>,
    private val to: (A, RegistryEntryLookup<E>) -> B,
    private val from: (B, RegistryEntryLookup<E>) -> A
) : Codec<B> {
    private fun getLookup(ops: DynamicOps<*>): RegistryEntryLookup<E>? =
        (ops as? RegistryOps)?.getEntryLookup(registry)?.getOrNull()

    override fun <T> encode(input: B, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        val lookup = getLookup(ops) ?: return DataResult.error { "Can't access registry $registry" }
        return codec.encode(from(input, lookup), ops, prefix)
    }

    override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<DFPair<B, T>> {
        val lookup = getLookup(ops) ?: return DataResult.error { "Can't access registry $registry" }
        return codec.decode(ops, input).map { (input, data) -> DFPair(to(input, lookup), data) }
    }
}

fun <A, B, E> Codec<A>.registryXmap(
    registry: RegistryKey<Registry<E>>,
    to: (A, RegistryEntryLookup<E>) -> B,
    from: (B, RegistryEntryLookup<E>) -> A
) = RegistryAwareXmapCodec(registry, this, to, from)

fun ItemPredicate(init: ItemPredicate.Builder.() -> Unit): ItemPredicate = ItemPredicate.Builder.create().apply(init).build()

fun <T, K: Any, V: Any> Stream<T>.associateNotNullToMap(transform: (T) -> Pair<K?, V?>?): Map<K, V> = this
    .map { transform(it) }
    .filter { it != null && it.first != null && it.second != null }
    .collect(Collectors.toMap({ it!!.first!! }, { it!!.second!! }))
