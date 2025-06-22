package archives.tater.rpgskills.util

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryOps
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import java.util.*
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KMutableProperty1
import com.mojang.datafixers.util.Unit as DFUnit

interface MutationCodec<A> : Encoder<A> {
    fun <T> update(target: A, ops: DynamicOps<T>, input: T): DataResult<*>

    override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T>

    fun codec(createDefault: () -> A) = object : Codec<A> {
        override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> =
            this@MutationCodec.encode(input, ops, prefix)

        override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> {
            return createDefault().apply {
                var error: DataResult.Error<*>? = null
                this@MutationCodec.update(this, ops, input).ifError { error = it }
                error?.let { return DataResult.error(it.messageSupplier, Pair(this, input), it.lifecycle) }
            }.let {
                DataResult.success(Pair(it, input))
            }
        }
    }
}

fun <A, C: MutableCollection<A>> Codec<A>.mutateCollection() = object : MutationCodec<C> {
    private val listCodec = listOf()

    override fun <T> update(target: C, ops: DynamicOps<T>, input: T): DataResult<*> =
        listCodec.decode(ops, input).ifSuccess {
            target.clear()
            target.addAll(it.first)
        }

    @Suppress("UNCHECKED_CAST")
    override fun <T> encode(input: C, ops: DynamicOps<T>, prefix: T): DataResult<T> =
        listCodec.encode(input as? List<A> ?: input.toList(), ops, prefix)
}

fun <K, V> Codec<Map<K, V>>.mutate() = object : MutationCodec<MutableMap<K, V>> {
    override fun <T> update(target: MutableMap<K, V>, ops: DynamicOps<T>, input: T): DataResult<*> =
        this@mutate.decode(ops, input).ifSuccess {
            target.clear()
            target.putAll(it.first)
        }

    override fun <T> encode(input: MutableMap<K, V>, ops: DynamicOps<T>, prefix: T): DataResult<T> =
        this@mutate.encode(input, ops, prefix)
}

abstract class RecordMutationCodec<A> : CompressorHolder(), MapEncoder<A> {
    abstract fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: A): DataResult<*>

    abstract override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T>

    abstract override fun <T> keys(ops: DynamicOps<T>): Stream<T>

    fun codec() = object : MutationCodec<A> {
        override fun <T> update(target: A, ops: DynamicOps<T>, input: T): DataResult<*> =
            ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap {
                this@RecordMutationCodec.update(ops, it, target)
            }

        override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> {
            return this@RecordMutationCodec.encode(input, ops, this@RecordMutationCodec.compressedBuilder(ops)).build(prefix)
        }
    }
}

fun <M, A> MutationCodec<A>.fieldFor(name: String, getValue: M.() -> A) = object : RecordMutationCodec<M>() {
    override fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: M): DataResult<*> {
        val value = input[name] ?: return DataResult.error<DFUnit> { "No key $name in $input" }
        return this@fieldFor.update(target.getValue(), ops, value)
    }

    override fun <T> encode(input: M, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> =
        prefix.add(name, this@fieldFor.encodeStart(ops, input.getValue()))

    override fun <T> keys(ops: DynamicOps<T>): Stream<T> = Stream.of(ops.createString(name))

}

fun <M, A> MapCodec<A>.forAccess(getValue: M.() -> A, setValue: M.(A) -> Unit) = object : RecordMutationCodec<M>() {
    override fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: M): DataResult<*> =
        this@forAccess.decode(ops, input).ifSuccess {
            target.setValue(it)
        }

    override fun <T> encode(input: M, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> =
        this@forAccess.encode(input.getValue(), ops, prefix)

    override fun <T> keys(ops: DynamicOps<T>): Stream<T> = this@forAccess.keys(ops)
}

@JvmName("nullableForAccess")
fun <M, A: Any> MapCodec<Optional<A>>.forAccess(getValue: M.() -> A?, setValue: M.(A?) -> Unit) = object : RecordMutationCodec<M>() {
    override fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: M): DataResult<*> =
        this@forAccess.decode(ops, input).ifSuccess {
            target.setValue(it.getOrNull())
        }

    override fun <T> encode(input: M, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> =
        this@forAccess.encode(Optional.ofNullable(input.getValue()), ops, prefix)

    override fun <T> keys(ops: DynamicOps<T>): Stream<T> = this@forAccess.keys(ops)
}

fun <M, A> MapCodec<A>.forAccess(property: KMutableProperty1<M, A>) = forAccess(property, property::set)

@JvmName("nullableForAccess")
fun <M, A: Any> MapCodec<Optional<A>>.forAccess(property: KMutableProperty1<M, A?>) = forAccess(property, property::set)

fun <A> RecordMutationCodec(vararg codecs: RecordMutationCodec<A>) = object : RecordMutationCodec<A>() {
    override fun <T> update(ops: DynamicOps<T>, input: MapLike<T>, target: A): DataResult<*> {
        val errors = codecs
            .map { it.update(ops, input, target) }
            .filter { it.isError }
        return if (errors.isEmpty()) DataResult.success(DFUnit.INSTANCE) else DataResult.error<DFUnit> {
            "Some or all fields could not be read: ${errors.joinToString(", ") { (it as DataResult.Error).message() }}"
        }
    }

    override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> {
        for (codec in codecs)
            codec.encode(input, ops, prefix)
        return prefix
    }

    override fun <T> keys(ops: DynamicOps<T>): Stream<T> = Arrays.stream(codecs).flatMap { it.keys(ops) }
}

fun <A> recordMutationCodec(vararg codecs: RecordMutationCodec<A>) = RecordMutationCodec(*codecs).codec()

fun <A> MutationCodec<A>.update(input: A, tag: NbtCompound, ops: DynamicOps<NbtElement> = NbtOps.INSTANCE) =
    update(input, ops, tag)

fun <A> MutationCodec<A>.update(input: A, tag: NbtCompound, registryLookup: WrapperLookup) =
    update(input, tag, RegistryOps.of(NbtOps.INSTANCE, registryLookup))

fun <A> MutationCodec<A>.encode(input: A, tag: NbtCompound, ops: DynamicOps<NbtElement> = NbtOps.INSTANCE): DataResult<*> =
    encode(input, ops, tag).flatMap {
        val compound = it as? NbtCompound ?: return@flatMap DataResult.error({ "$it was not an NbtCompound" }, it)
        for (key in compound.keys)
            tag.put(key, compound[key])
        DataResult.success(it)
    }

fun <A> MutationCodec<A>.encode(input: A, tag: NbtCompound, registryLookup: WrapperLookup) =
    encode(input, tag, RegistryOps.of(NbtOps.INSTANCE, registryLookup))

