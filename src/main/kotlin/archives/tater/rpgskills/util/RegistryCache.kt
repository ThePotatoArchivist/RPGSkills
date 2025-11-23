package archives.tater.rpgskills.util

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import java.util.WeakHashMap

class RegistryCache<K, V>(private val registryRef: RegistryKey<Registry<V>>, private val getKeys: (V) -> Iterable<K>) : RegistryEntryAddedCallback<V>, DynamicRegistrySetupCallback {
    private val cache: MutableMap<K, V> = WeakHashMap()

    operator fun get(key: K): V? =
        cache[key]

    override fun onEntryAdded(rawId: Int, id: Identifier, value: V) {
        for (key in getKeys(value))
            cache[key] = value
    }

    override fun onRegistrySetup(registryView: DynamicRegistryView) {
        registryView.registerEntryAdded<V>(registryRef, this)
    }
}