package archives.tater.rpgskills.util

import com.google.common.collect.HashMultimap
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
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
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.Pair
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import com.mojang.datafixers.util.Pair as DFPair

internal fun <T, C> field(name: String, getter: (C) -> T, codec: Codec<T>): RecordCodecBuilder<C, T> =
    codec.fieldOf(name).forGetter(getter)

internal fun <T, C> field(name: String, getter: (C) -> T, codec: MapCodec<T>): RecordCodecBuilder<C, T> =
    codec.fieldOf(name).forGetter(getter)

internal fun <T, C> field(name: String, getter: (C) -> T, default: T, codec: Codec<T>): RecordCodecBuilder<C, T> =
    codec.optionalFieldOf(name, default).forGetter(getter)

internal fun <T: Any, C> optionalField(name: String, getter: (C) -> T?, codec: Codec<T>): RecordCodecBuilder<C, Optional<T>> =
    codec.optionalFieldOf(name).forGetter { Optional.ofNullable(getter(it)) }

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