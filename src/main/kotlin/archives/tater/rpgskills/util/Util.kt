package archives.tater.rpgskills.util

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

internal fun <T, C> field(name: String, getter: (C) -> T, codec: Codec<T>): RecordCodecBuilder<C, T> = codec.fieldOf(name).forGetter(getter)
internal fun <T, C> field(name: String, getter: (C) -> T, codec: MapCodec<T>): RecordCodecBuilder<C, T> = codec.fieldOf(name).forGetter(getter)
internal fun <T, C> field(name: String, getter: (C) -> T, default: T, codec: Codec<T>): RecordCodecBuilder<C, T> = codec.optionalFieldOf(name, default).forGetter(getter)
