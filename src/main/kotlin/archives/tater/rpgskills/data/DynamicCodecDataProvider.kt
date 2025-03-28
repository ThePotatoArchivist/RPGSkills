package archives.tater.rpgskills.data

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.data.DataOutput.OutputType
import net.minecraft.data.DataProvider
import net.minecraft.data.DataWriter
import net.minecraft.registry.RegistryOps
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

abstract class DynamicCodecDataProvider<T>(
    dataOutput: FabricDataOutput,
    outputType: OutputType,
    directoryName: String,
    private val codec: Codec<T>,
    private val registriesFuture: CompletableFuture<WrapperLookup>,
) : DataProvider {

    private val pathResolver = dataOutput.getResolver(outputType, directoryName)

    override fun run(writer: DataWriter): CompletableFuture<*> {
        return registriesFuture.thenApply { registries ->
            val entries: MutableMap<Identifier, JsonElement> = HashMap()

            configure({ id, value ->
                val existingJson = entries.put(id, convert(registries, id, value))
                require(existingJson == null) { "Duplicate entry $id" }
            }, registries)
            write(writer, entries).join()
        }
    }

    /**
     * Implement this method to register entries to generate.
     *
     * @param provider A consumer that accepts an [Identifier] and a value to register.
     */
    protected abstract fun configure(provider: BiConsumer<Identifier, T>, registries: WrapperLookup)

    private fun convert(registries: WrapperLookup, id: Identifier, value: T): JsonElement {
        val dataResult = codec.encodeStart(RegistryOps.of(JsonOps.INSTANCE, registries), value)
        return dataResult.get()
            .mapRight { "Invalid entry $id: ${it.message()}" }
            .orThrow()
    }

    private fun write(writer: DataWriter, entries: Map<Identifier, JsonElement>): CompletableFuture<*> {
        return CompletableFuture.allOf(*entries.entries.stream().map { (id, json) ->
            DataProvider.writeToPath(writer, json, pathResolver.resolveJson(id))
        }.toArray { Array<CompletableFuture<*>?>(it) { null } })
    }
}