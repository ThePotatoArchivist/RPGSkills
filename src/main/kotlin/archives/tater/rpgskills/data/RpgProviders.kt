package archives.tater.rpgskills.data

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
import net.minecraft.data.DataOutput.OutputType
import net.minecraft.registry.*
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

private fun dynamicRegistryPath(key: RegistryKey<*>) = "${key.value.namespace}/${key.value.path}"

abstract class SkillProvider(
    dataOutput: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricCodecDataProvider<Skill>(dataOutput, registriesFuture, OutputType.DATA_PACK, dynamicRegistryPath(Skill.key), Skill.CODEC) {
	override fun getName(): String = "Skills"
}

abstract class LockGroupProvider(
    dataOutput: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricCodecDataProvider<LockGroup>(dataOutput, registriesFuture, OutputType.DATA_PACK, dynamicRegistryPath(LockGroup.key), LockGroup.CODEC) {
    override fun getName(): String = "Lock Groups"
}

abstract class ClassProvider(
    dataOutput: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricCodecDataProvider<SkillClass>(dataOutput, registriesFuture, OutputType.DATA_PACK, dynamicRegistryPath(SkillClass.key), SkillClass.CODEC) {
    override fun getName(): String = "Skill Classes"
}

abstract class JobProvider(
    dataOutput: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricCodecDataProvider<Job>(dataOutput, registriesFuture, OutputType.DATA_PACK, dynamicRegistryPath(Job.key), Job.CODEC) {
    override fun getName(): String = "Jobs"
}

class BuildEntry<T>(
    val registry: RegistryKey<out Registry<T>>,
    val id: Identifier,
    getValue: () -> T,
) {
    val value by lazy(getValue)
    val key: RegistryKey<T> = RegistryKey.of(registry, id)
    lateinit var entry: RegistryEntry<T>
}

fun <T> BiConsumer<Identifier, T>.accept(buildEntry: BuildEntry<T>) {
    accept(buildEntry.id, buildEntry.value)
}

interface BuildsRegistry<T> {
    val registry: RegistryKey<out Registry<T>>

    fun BuildEntry(id: Identifier, value: () -> T) = BuildEntry(registry, id, value)

    fun buildRegistry(registryBuilder: RegistryBuilder) {
        registryBuilder.addRegistry(registry, ::bootstrap)
    }

    fun bootstrap(registerable: Registerable<T>)

    fun Registerable<T>.register(buildEntry: BuildEntry<T>) {
        buildEntry.entry = register(RegistryKey.of(registry, buildEntry.id), buildEntry.value)
    }
}
