package archives.tater.rpgskills.data

import archives.tater.rpgskills.util.registryXmap
import archives.tater.rpgskills.util.value
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryEntryLookup
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.registry.entry.RegistryFixedCodec
import net.minecraft.registry.tag.TagKey
import java.util.*
import java.util.function.Predicate
import java.util.stream.Stream


abstract class RegistryIngredient<T> : Predicate<RegistryEntry<T>> {

    val matchingEntries: List<RegistryEntry<T>> by lazy { findMatchingEntries().toList() }
    val matchingValues: List<T> by lazy { matchingEntries.stream().map { it.value }.distinct().toList() }

    val size get() = matchingEntries.size
    val isEmpty get() = matchingEntries.isEmpty()

    protected abstract fun findMatchingEntries(): Stream<RegistryEntry<T>>

    @JvmName("testValue")
    fun test(value: T): Boolean = value in matchingValues

    override fun test(value: RegistryEntry<T>): Boolean = value in matchingEntries

    sealed interface Entry<T> {
        val matchingEntries: List<RegistryEntry<T>>
        val matchingValues: List<T>
    }

    data class DirectEntry<T>(val entry: RegistryEntry<T>) : RegistryIngredient<T>(), Entry<T> {
        override fun findMatchingEntries(): Stream<RegistryEntry<T>> = Stream.of(entry)

        companion object {
            fun <T> createCodec(registry: RegistryKey<Registry<T>>): Codec<DirectEntry<T>> = RegistryFixedCodec.of(registry).xmap({ DirectEntry(it) }, { it.entry })
        }
    }

    data class TagEntry<T>(private val registry: RegistryEntryLookup<T>, val tag: TagKey<T>) : RegistryIngredient<T>(), Entry<T> {
        override fun findMatchingEntries(): Stream<RegistryEntry<T>> = registry.getOrThrow(tag).stream()

        companion object {
            fun <T> createCodec(registry: RegistryKey<Registry<T>>): Codec<TagEntry<T>> =
                TagKey.codec(registry).registryXmap(registry,
                    { it, registry -> TagEntry(registry, it) },
                    { it, _ -> it.tag }
                )
        }
    }

    data class Composite<T>(private val registry: RegistryEntryLookup<T>, val entries: List<Entry<T>> = listOf()) : RegistryIngredient<T>() {
        override fun findMatchingEntries(): Stream<RegistryEntry<T>> =
            entries.stream().flatMap { it.matchingEntries.stream() }.distinct()

        companion object {
            fun <T> createCodec(registry: RegistryKey<Registry<T>>): Codec<Composite<T>> = Codec.either(DirectEntry.createCodec(registry), TagEntry.createCodec(registry)).listOf().registryXmap(
                registry,
                { entries, registry -> Composite(registry, entries.map { either -> either.map({ it }, { it }) }) },
                { composite, _ -> composite.entries.map { when (it) {
                    is DirectEntry -> Either.left(it)
                    is TagEntry -> Either.right(it)
                } } }
            )
        }
    }

    class Builder<T> private constructor(val registryEntries: RegistryEntryLookup<T>, val registry: Registry<T>?) {
        private val entries = mutableListOf<RegistryEntry<T>>()
        private val tags = mutableListOf<TagKey<T>>()

        constructor(registry: RegistryEntryLookup<T>) : this(registry, null)
        constructor(registry: Registry<T>) : this(registry.readOnlyWrapper, registry)

        fun add(entry: RegistryEntry<T>) {
            entries.add(entry)
        }

        fun add(key: RegistryKey<T>) {
            add(registryEntries.getOrThrow(key))
        }

        fun add(value: T) {
            add((registry ?: throw Exception("Registry not provided for reverse lookup")).getEntry(value))
        }

        fun add(tagKey: TagKey<T>) {
            tags.add(tagKey)
        }

        fun build() = Composite(registryEntries, entries.map(::DirectEntry) + tags.map { TagEntry(registryEntries, it) })

        operator fun RegistryEntry<T>.unaryPlus() = add(this)
        operator fun RegistryKey<T>.unaryPlus() = add(this)
        operator fun T.unaryPlus() = add(this)
        operator fun TagKey<T>.unaryPlus() = add(this)
    }

    companion object {
        fun <T> of(registry: RegistryEntryLookup<T>, vararg entries: Entry<T>): RegistryIngredient<T> = Composite(registry, entries.toList())
        fun <T> of(registry: RegistryEntryLookup<T>, init: Builder<T>.() -> Unit) = Builder(registry).apply(init).build()
        fun <T> of(registry: Registry<T>, init: Builder<T>.() -> Unit) = Builder(registry).apply(init).build()

        fun ofItems(init: Builder<Item>.() -> Unit) = of(Registries.ITEM, init)
        fun ofBlocks(init: Builder<Block>.() -> Unit) = of(Registries.BLOCK, init)
        fun ofEntities(init: Builder<EntityType<*>>.() -> Unit) = of(Registries.ENTITY_TYPE, init)
        fun <T> of(lookup: RegistryWrapper.WrapperLookup, registry: RegistryKey<Registry<T>>, init: Builder<T>.() -> Unit) = of(lookup.getWrapperOrThrow(registry), init)

        fun <T> createCodec(registry: RegistryKey<Registry<T>>) = Composite.createCodec(registry)

        val EMPTY = Composite<Any>(object : RegistryWrapper<Any> {
            override fun getOptional(key: RegistryKey<Any>?): Optional<RegistryEntry.Reference<Any>> =
                Optional.empty()

            override fun getOptional(tag: TagKey<Any>?): Optional<RegistryEntryList.Named<Any>> = Optional.empty()

            override fun streamEntries(): Stream<RegistryEntry.Reference<Any>> = Stream.empty()

            override fun streamTags(): Stream<RegistryEntryList.Named<Any>> = Stream.empty()
        })

        @Suppress("UNCHECKED_CAST")
        fun <T> empty() = EMPTY as Composite<T>
    }
}
