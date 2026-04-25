package archives.tater.rpgskills.util

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.component.ComponentHolder
import net.minecraft.component.ComponentType
import net.minecraft.util.Util.memoize

@JvmRecord
data class ComponentValues<T>(
    val type: ComponentType<T>,
    val values: Set<T>,
) {
    fun contains(components: ComponentHolder) = type in components && components[type] in values

    companion object {
        private fun <T> createCodec(type: ComponentType<T>): MapCodec<ComponentValues<T>> =
            type.codecOrThrow.listOf().xmap(List<T>::toSet, Set<T>::toList).fieldOf("values").xmap({ ComponentValues(type, it) }, ComponentValues<T>::values)

        @Suppress("UNCHECKED_CAST")
        private val CREATE_CODEC = memoize<ComponentType<*>, MapCodec<ComponentValues<*>>> { createCodec(it) as MapCodec<ComponentValues<*>> }

        val CODEC: Codec<ComponentValues<*>> = ComponentType.PERSISTENT_CODEC.dispatch(ComponentValues<*>::type) { CREATE_CODEC.apply(it) }
    }
}