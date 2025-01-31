package archives.tater.rpgskills.data

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
import net.minecraft.data.DataOutput
import net.minecraft.registry.Registerable
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import java.util.function.BiConsumer

abstract class SkillProvider(
    dataOutput: FabricDataOutput,
) : FabricCodecDataProvider<Skill>(dataOutput, DataOutput.OutputType.DATA_PACK, "rpgskills/skills", Skill.CODEC) {
	override fun getName(): String = "Skills"
}

abstract class LockGroupProvider(
    dataOutput: FabricDataOutput,
) : FabricCodecDataProvider<LockGroup>(dataOutput, DataOutput.OutputType.DATA_PACK, "rpgskills/lockgroup", LockGroup.CODEC) {
    override fun getName(): String = "Lock Group"
}

class Entry<T>(
    val registry: RegistryKey<out Registry<T>>,
    val id: Identifier,
    val value: T,
) {
    val key: RegistryKey<T> = RegistryKey.of(registry, id)
}

fun <T> BiConsumer<Identifier, T>.accept(entry: Entry<T>) {
    accept(entry.id, entry.value)
}

interface BuildsRegistry<T> {
    val registry: RegistryKey<out Registry<T>>

    fun Entry(id: Identifier, value: T) = Entry(registry, id, value)

    fun buildRegistry(registryBuilder: RegistryBuilder) {
        registryBuilder.addRegistry(registry, ::bootstrap)
    }

    fun bootstrap(registerable: Registerable<T>)

    fun Registerable<T>.register(entry: Entry<T>) {
        register(RegistryKey.of(registry, entry.id), entry.value)
    }
}
