package archives.tater.rpgskills.data

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.registry.tag.TagKey
import java.util.*
import java.util.function.Predicate
import java.util.stream.Stream


sealed interface RegistryIngredient<T> : Predicate<RegistryEntry<T>> {

    fun findMatchingEntries(lookup: RegistryWrapper<T>): List<RegistryEntry<T>>

    data class EntryEntry<T>(private val entry: RegistryEntry<T>) : RegistryIngredient<T> {
        override fun findMatchingEntries(lookup: RegistryWrapper<T>): List<RegistryEntry<T>> = listOf(entry)

        @Suppress("DEPRECATION")
        override fun test(value: RegistryEntry<T>): Boolean = entry.matches(value)

        companion object {
            fun <T> createCodec(registry: Registry<T>): Codec<EntryEntry<T>> = registry.entryCodec.xmap({ EntryEntry(it) }, { it.entry })
        }
    }

    data class TagEntry<T>(private val tag: TagKey<T>) : RegistryIngredient<T> {
        override fun findMatchingEntries(lookup: RegistryWrapper<T>): List<RegistryEntry<T>> =
            lookup.streamEntries().filter { it.isIn(tag) }.toList()

        override fun test(value: RegistryEntry<T>): Boolean = value.isIn(tag)

        companion object {
            fun <T> createCodec(registryRef: RegistryKey<out Registry<T>>): Codec<TagEntry<T>> = TagKey.codec(registryRef).xmap({ TagEntry(it) }, { it.tag })
        }
    }

    class Composite<T>(private val registry: RegistryWrapper<T>, private val entries: List<RegistryIngredient<T>> = listOf()) : RegistryIngredient<T> {
        val matchingEntries: List<RegistryEntry<T>> by lazy { findMatchingEntries(registry) }
        val matchingValues: List<T> by lazy { matchingEntries.stream().map { it.value() }.distinct().toList() }

        val size get() = matchingEntries.size
        val isEmpty get() = matchingEntries.isEmpty()

        override fun findMatchingEntries(lookup: RegistryWrapper<T>): List<RegistryEntry<T>> =
            entries.stream().flatMap { it.findMatchingEntries(lookup).stream() }.distinct().toList()

        override fun test(value: RegistryEntry<T>): Boolean = value in matchingEntries

        @JvmName("testValue")
        fun test(value: T): Boolean = value in matchingValues

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return entries == (other as Composite<*>).entries
        }

        override fun hashCode(): Int {
            return entries.hashCode()
        }

        companion object {
            fun <T> createCodec(registry: Registry<T>): Codec<Composite<T>> = Codec.either(EntryEntry.createCodec(registry), TagEntry.createCodec(registry.key)).listOf().xmap(
                { entries -> Composite(registry.readOnlyWrapper, entries.map { either -> either.map({ it }, { it }) }) },
                { composite -> composite.entries.map { when (it) {
                    is EntryEntry -> Either.left(it)
                    is TagEntry -> Either.right(it)
                    else -> throw AssertionError("Cannot encode nested composites")
                } } }
            )
        }
    }

    class Builder<T>(val registry: Registry<T>) {
        private val entries = mutableListOf<RegistryEntry<T>>()
        private val tags = mutableListOf<TagKey<T>>()

        fun add(entry: RegistryEntry<T>) {
            entries.add(entry)
        }

        fun add(value: T) {
            add(registry.getEntry(value))
        }

        fun add(tagKey: TagKey<T>) {
            tags.add(tagKey)
        }

        fun build() = Composite(registry.readOnlyWrapper, entries.map(::EntryEntry) + tags.map(::TagEntry))

        operator fun RegistryEntry<T>.unaryPlus() = add(this)
        operator fun T.unaryPlus() = add(this)
        operator fun TagKey<T>.unaryPlus() = add(this)
    }

    companion object {
        fun <T> of(registry: RegistryWrapper<T>, vararg entries: RegistryIngredient<T>): RegistryIngredient<T> = Composite(registry, entries.toList())
        fun <T> of(registry: Registry<T>, init: Builder<T>.() -> Unit) = Builder(registry).apply(init).build()

        fun ofItems(init: Builder<Item>.() -> Unit) = of(Registries.ITEM, init)
        fun ofBlocks(init: Builder<Block>.() -> Unit) = of(Registries.BLOCK, init)
        fun ofEntities(init: Builder<EntityType<*>>.() -> Unit) = of(Registries.ENTITY_TYPE, init)

        fun <A> createCodec(registry: Registry<A>) = Composite.createCodec(registry)

        fun <T> empty() = Composite(object : RegistryWrapper<T> {
            override fun getOptional(key: RegistryKey<T>?): Optional<RegistryEntry.Reference<T>> =
                Optional.empty()

            override fun getOptional(tag: TagKey<T>?): Optional<RegistryEntryList.Named<T>> = Optional.empty()

            override fun streamEntries(): Stream<RegistryEntry.Reference<T>> = Stream.empty()

            override fun streamTags(): Stream<RegistryEntryList.Named<T>> = Stream.empty()
        })

    }
}
