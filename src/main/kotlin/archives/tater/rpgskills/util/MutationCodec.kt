package archives.tater.rpgskills.util

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KMutableProperty1

interface MutationCodec<A> : Encoder<A> {
    fun <T> update(ops: DynamicOps<T>, input: T, target: A)

    override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T>

    fun codec(createDefault: () -> A) = object : Codec<A> {
        override fun <T : Any?> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> =
            this@MutationCodec.encode(input, ops, prefix)

        override fun <T : Any?> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> =
            DataResult.success(Pair(createDefault().apply { // Sketchy, no error
                this@MutationCodec.update(ops, input, this)
            }, input))
    }
}

fun <A, C: MutableCollection<A>> Codec<A>.mutateCollection() = object : MutationCodec<C> {
    private val listCodec = listOf()

    override fun <T> update(ops: DynamicOps<T>, input: T, target: C) {
        listCodec.decode(ops, input).ifSuccess {
            target.clear()
            target.addAll(it.first)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> encode(input: C, ops: DynamicOps<T>, prefix: T): DataResult<T> =
        listCodec.encode(input as? List<A> ?: input.toList(), ops, prefix)
}

fun <K, V> Codec<Map<K, V>>.mutateMap() = object : MutationCodec<MutableMap<K, V>> {
    override fun <T> update(ops: DynamicOps<T>, input: T, target: MutableMap<K, V>) {
        this@mutateMap.decode(ops, input).ifSuccess {
            target.clear()
            for ((key, value) in it.first)
                target[key] = value
        }
    }

    override fun <T : Any?> encode(input: MutableMap<K, V>, ops: DynamicOps<T>, prefix: T): DataResult<T> =
        this@mutateMap.encode(input, ops, prefix)
}

abstract class RecordMutationCodec<A> : CompressorHolder(), MapEncoder<A> {
    abstract fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: A)

    abstract override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T>

    abstract override fun <T> keys(ops: DynamicOps<T>): Stream<T>

    fun codec() = object : MutationCodec<A> {
        override fun <T> update(ops: DynamicOps<T>, input: T, target: A) {
            ops.getMap(input).setLifecycle(Lifecycle.stable()).ifSuccess {
                this@RecordMutationCodec.update(ops, it, target)
            }
        }

        override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> {
            return this@RecordMutationCodec.encode(input, ops, this@RecordMutationCodec.compressedBuilder(ops)).build(prefix)
        }
    }
}

fun <M, A> MutationCodec<A>.fieldFor(name: String, getValue: M.() -> A) = object : RecordMutationCodec<M>() {
    override fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: M) {
        val value = input[name] ?: return // TODO error message
        this@fieldFor.update(ops, value, target.getValue())
    }

    override fun <T> encode(input: M, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> =
        prefix.add(name, this@fieldFor.encodeStart(ops, input.getValue()))

    override fun <T> keys(ops: DynamicOps<T>): Stream<T> = Stream.of(ops.createString(name))

}

fun <M, A> MapCodec<A>.forAccess(getValue: M.() -> A, setValue: M.(A) -> Unit) = object : RecordMutationCodec<M>() {
    override fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: M) {
        this@forAccess.decode(ops, input).ifSuccess {
            target.setValue(it)
        }
    }

    override fun <T> encode(input: M, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> =
        this@forAccess.encode(input.getValue(), ops, prefix)

    override fun <T> keys(ops: DynamicOps<T>): Stream<T> = this@forAccess.keys(ops)
}

fun <M, A> MapCodec<A>.forAccess(property: KMutableProperty1<M, A>) = forAccess(property, property::set)

fun <A> RecordMutationCodec(vararg codecs: RecordMutationCodec<A>) = object : RecordMutationCodec<A>() {
    override fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: A) {
        for (codec in codecs)
            codec.update(ops, input, target)
    }

    override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> {
        for (codec in codecs)
            codec.encode(input, ops, prefix)
        return prefix
    }

    override fun <T> keys(ops: DynamicOps<T>): Stream<T> = Arrays.stream(codecs).flatMap { it.keys(ops) }
}

fun <A> recordMutationCodec(vararg codecs: RecordMutationCodec<A>) = RecordMutationCodec(*codecs).codec()