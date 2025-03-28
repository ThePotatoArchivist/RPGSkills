package archives.tater.rpgskills.data

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
import net.minecraft.data.DataOutput
import net.minecraft.registry.*
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

abstract class SkillProvider(
    dataOutput: FabricDataOutput,
) : FabricCodecDataProvider<Skill>(dataOutput, DataOutput.OutputType.DATA_PACK, "rpgskills/skills", Skill.CODEC) {
	override fun getName(): String = "Skills"
}

abstract class LockGroupProvider(
    dataOutput: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : DynamicCodecDataProvider<LockGroup>(dataOutput, DataOutput.OutputType.DATA_PACK, "rpgskills/lockgroup", LockGroup.CODEC, registriesFuture) {
    override fun getName(): String = "Lock Group"
}

class BuildEntry<T>(
    val registry: RegistryKey<out Registry<T>>,
    val id: Identifier,
    val value: T,
) {
    val key: RegistryKey<T> = RegistryKey.of(registry, id)
    lateinit var entry: RegistryEntry<T>
}

fun <T> BiConsumer<Identifier, T>.accept(buildEntry: BuildEntry<T>) {
    accept(buildEntry.id, buildEntry.value)
}

interface BuildsRegistry<T> {
    val registry: RegistryKey<out Registry<T>>

    fun BuildEntry(id: Identifier, value: T) = BuildEntry(registry, id, value)

    fun buildRegistry(registryBuilder: RegistryBuilder) {
        registryBuilder.addRegistry(registry, ::bootstrap)
    }

    fun bootstrap(registerable: Registerable<T>)

    fun Registerable<T>.register(buildEntry: BuildEntry<T>) {
        buildEntry.entry = register(RegistryKey.of(registry, buildEntry.id), buildEntry.value)
    }
}
