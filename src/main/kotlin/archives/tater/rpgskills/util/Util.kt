package archives.tater.rpgskills.util

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

internal fun <T, C> field(name: String, getter: (C) -> T, codec: Codec<T>): RecordCodecBuilder<C, T> = codec.fieldOf(name).forGetter(getter)
internal fun <T, C> field(name: String, getter: (C) -> T, codec: MapCodec<T>): RecordCodecBuilder<C, T> = codec.fieldOf(name).forGetter(getter)
internal fun <T, C> field(name: String, getter: (C) -> T, default: T, codec: Codec<T>): RecordCodecBuilder<C, T> = codec.optionalFieldOf(name, default).forGetter(getter)

internal inline val <T> RegistryEntry<T>.value get() = value()

fun IdentifiableResourceReloadListener(fabricId: Identifier, reload: (
    synchronizer: ResourceReloader.Synchronizer,
    manager: ResourceManager,
    trepareProfiler: Profiler,
    applyProfiler: Profiler,
    prepareExecutor: Executor,
    applyExecutor: Executor
) -> CompletableFuture<Void>) = object : IdentifiableResourceReloadListener {
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

fun SimpleSynchronousResourceReloadListener(fabricId: Identifier, reload: (ResourceManager) -> Unit) = object : SimpleSynchronousResourceReloadListener {
    override fun reload(manager: ResourceManager) {
        reload(manager)
    }

    override fun getFabricId(): Identifier = fabricId

}

interface RegistryKeyHolder<T> {
    val key: RegistryKey<T>
}

operator fun <E> DynamicRegistryManager.get(holder: RegistryKeyHolder<out Registry<out E>?>): Registry<E> =
    this[holder.key]
