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

class BuildEntry<T: Any>(
    val registry: RegistryKey<out Registry<T>>,
    val id: Identifier,
    private val create: (Registerable<T>) -> T,
) {
    lateinit var value: T
    val key: RegistryKey<T> = RegistryKey.of(registry, id)
    lateinit var entry: RegistryEntry<T>

    fun getValue(registerable: Registerable<T>): T {
        value = create(registerable)
        return value
    }
}

fun <T: Any> BiConsumer<Identifier, T>.accept(buildEntry: BuildEntry<T>) {
    accept(buildEntry.id, buildEntry.value)
}

interface BuildsRegistry<T: Any> {
    val registry: RegistryKey<out Registry<T>>

    fun BuildEntry(id: Identifier, value: () -> T) = BuildEntry(registry, id) { value() }
    fun depBuildEntry(id: Identifier, create: (Registerable<T>) -> T) = BuildEntry(registry, id, create)

    fun buildRegistry(registryBuilder: RegistryBuilder) {
        registryBuilder.addRegistry(registry, ::bootstrap)
    }

    fun bootstrap(registerable: Registerable<T>)

    fun Registerable<T>.register(buildEntry: BuildEntry<T>) {
        buildEntry.entry = register(RegistryKey.of(registry, buildEntry.id), buildEntry.getValue(this))
    }
}

operator fun <S> Registerable<*>.get(registryRef: RegistryKey<out Registry<S>>): RegistryEntryLookup<S> =
    getRegistryLookup(registryRef)

operator fun <S: Any> Registerable<*>.get(buildEntry: BuildEntry<S>): RegistryEntry<S> = this[buildEntry.registry].getOrThrow(buildEntry.key)