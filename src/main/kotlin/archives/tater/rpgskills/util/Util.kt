package archives.tater.rpgskills.util

import archives.tater.rpgskills.RPGSkills
import com.google.common.collect.HashMultimap
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.TrackedData
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.Pair
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

interface ComponentKeyHolder<C : Component, T> {
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