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
import kotlin.jvm.optionals.getOrNull
import kotlin.math.log

abstract class CodecConfig<T: Any>(private val unsuffixedPath: String, private val logger: Logger = LoggerFactory.getLogger(unsuffixedPath)) {
    abstract val codec: MutationCodec<T>

    abstract fun getDefault(): T

    fun load(): T {
        val config = getDefault()
        val configPath = FabricLoader.getInstance().configDir.resolve("$unsuffixedPath.json")
        if (!configPath.exists())
            codec.encodeStart(JsonOps.INSTANCE, config).ifSuccess {
                DataProvider.writeToPath(DataWriter.UNCACHED, it, configPath)
            }.ifError {
                logger.error("Failed to create config $unsuffixedPath: {}", it.message())
            }
        else if (!configPath.isRegularFile())
            logger.error("Failed to create config $unsuffixedPath, an incorrect file type with the same name exists")
        else {
            val input = JsonParser.parseReader(configPath.bufferedReader())
            codec.update(config, JsonOps.INSTANCE, input).ifError { error ->
                logger.error("Failed to read config $unsuffixedPath: {}", error.message())

                logger.info("Attempting to update config $unsuffixedPath")
                codec.encodeStart(JsonOps.INSTANCE, config).ifSuccess {
                    DataProvider.writeToPath(DataWriter.UNCACHED, it, configPath)
                }.ifError {
                    logger.error("Failed to update config: {}", it.message())
                }
            }
        }
        return config
    }
}