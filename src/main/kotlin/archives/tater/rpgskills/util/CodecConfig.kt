package archives.tater.rpgskills.util

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.data.DataProvider
import net.minecraft.data.DataWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

abstract class CodecConfig<T: Any>(private val unsuffixedPath: String, private val logger: Logger = LoggerFactory.getLogger(unsuffixedPath)) {
    abstract val codec: Codec<T>

    abstract val defaultConfig: T

    fun load(): T {
        val configPath = FabricLoader.getInstance().configDir.resolve("$unsuffixedPath.json")
        if (!configPath.exists())
            codec.encodeStart(JsonOps.INSTANCE, defaultConfig).ifSuccess {
                DataProvider.writeToPath(DataWriter.UNCACHED, it, configPath)
            }.ifError {
                logger.error("Failed to create config $unsuffixedPath", it.message())
            }
        else if (!configPath.isRegularFile())
            logger.error("Failed to create config $unsuffixedPath, an incorrect file type with the same name exists")
        else {
            val result = codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(configPath.bufferedReader())).ifError {
                logger.error("Failed to read config $unsuffixedPath", it.message())
            }.result()
            if (result.isPresent)
                return result.get()
        }
        return defaultConfig
    }
}