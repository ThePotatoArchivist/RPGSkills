package archives.tater.rpgskills.util

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import java.util.*

/**
 * A codec that has two codecs that deserialize to the same type
 */
class AlternateCodec<A>(
    private val first: Codec<A>,
    private val second: Codec<A>,
    private val useSecond: (A) -> Boolean,
) : Codec<A> {
    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> {
        val firstError = first.decode(ops, input).also {
            if (it.isSuccess) return it
        }
        val secondError = second.decode(ops, input).also {
            if (it.isSuccess) return it
        }
        return DataResult.error {
            "Failed to parse alternatives. First: ${firstError.error().orElseThrow().message()}; Second: ${secondError.error().orElseThrow().message()}"
        }
    }

    override fun <T : Any> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> =
        (if (useSecond(input)) second else first).encode(input, ops, prefix)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return (other as AlternateCodec<*>).let {
            first == it.first && second == it.second && useSecond == it.useSecond
        }
    }

    override fun hashCode() = Objects.hash(first, second, useSecond)

    override fun toString(): String = "AlternateCodec[$first, $second]"
}

fun <A> Codec<A>.singleOrList(): Codec<List<A>> =
    AlternateCodec(this.listOf(), this.xmap({ listOf(it) }, { it[0] })) { it.size == 1 }
