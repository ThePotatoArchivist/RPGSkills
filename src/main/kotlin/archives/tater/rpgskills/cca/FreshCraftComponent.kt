package archives.tater.rpgskills.cca

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.ComponentKeyHolder
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Unit as McUnit
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry

class FreshCraftComponent : Component {
    var freshCount = 0

    fun addFreshCount(count: Int) {
        freshCount += count
    }

    override fun readFromNbt(
        tag: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup
    ) {
        freshCount = tag.getInt("fresh_count")
    }

    override fun writeToNbt(
        tag: NbtCompound,
        registryLookup: RegistryWrapper.WrapperLookup
    ) {
        tag.putInt("fresh_count", freshCount)
    }

    companion object : ComponentKeyHolder<FreshCraftComponent> {
        override val key: ComponentKey<FreshCraftComponent> = ComponentRegistry.getOrCreate(RPGSkills.id("fresh_craft"), FreshCraftComponent::class.java)

        @JvmField val KEY = key

        @JvmField val IS_FRESH_CRAFT: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
    }
}