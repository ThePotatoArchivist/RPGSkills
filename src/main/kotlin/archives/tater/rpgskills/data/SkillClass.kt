package archives.tater.rpgskills.data

import archives.tater.rpgskills.RPGSkills
import archives.tater.rpgskills.util.RegistryKeyHolder
import archives.tater.rpgskills.util.SHORT_STACK_CODEC
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryFixedCodec

@JvmRecord
data class SkillClass(
    val name: String,
    val icon: ItemStack,
    val description: String,
    val startingLevels: Map<RegistryEntry<Skill>, Int>
) {
    constructor(
        name: String,
        icon: Item,
        description: String,
        startingLevels: Map<RegistryEntry<Skill>, Int>,
    ) : this(name, icon.defaultStack, description, startingLevels)

    val startingLevel get() = startingLevels.values.sum()

    companion object : RegistryKeyHolder<Registry<SkillClass>> {
        override val key: RegistryKey<Registry<SkillClass>> = RegistryKey.ofRegistry(RPGSkills.id("class"))

        val CODEC: Codec<SkillClass> = RecordCodecBuilder.create { it.group(
            Codec.STRING.fieldOf("name").forGetter(SkillClass::name),
            SHORT_STACK_CODEC.fieldOf("icon").forGetter(SkillClass::icon),
            Codec.STRING.fieldOf("description").forGetter(SkillClass::description),
            Codec.unboundedMap(RegistryFixedCodec.of(Skill.key), Codec.INT).fieldOf("starting_levels").forGetter(SkillClass::startingLevels)
        ).apply(it, ::SkillClass) }
    }
}
