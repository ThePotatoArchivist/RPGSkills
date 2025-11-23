package archives.tater.rpgskills.util

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.bettercombat.api.WeaponAttributesHelper.override
import java.util.WeakHashMap

class RegistryCache<K, V>(private val registryRef: RegistryKey<Registry<V>>, private val getKeys: (RegistryEntry.Reference<V>) -> Iterable<K>) {
    private var cache: MutableMap<RegistryWrapper.WrapperLookup, Map<K, RegistryEntry.Reference<V>>> = WeakHashMap()

    private fun generate(registryManager: RegistryWrapper.WrapperLookup): Map<K, RegistryEntry.Reference<V>> =
        WeakHashMap<K, RegistryEntry.Reference<V>>().apply {
            registryManager.getWrapperOrThrow(registryRef).streamEntries()
                .forEach {
                    for (key in getKeys(it))
                        this[key] = it
                }
        }

    operator fun get(registryManager: RegistryWrapper.WrapperLookup): Map<K, RegistryEntry.Reference<V>> =
        cache.getOrPut(registryManager) { generate(registryManager) }
}