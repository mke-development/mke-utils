package team.mke.utils.env

import org.slf4j.LoggerFactory
import ru.raysmith.utils.properties.PropertiesFactory
import ru.raysmith.utils.properties.getOrNull
import java.io.FileNotFoundException

/** Значение из env.properties */
val env by lazy { Environment.value }

/** Значение из env.properties */
enum class Environment {
    DEV, PROD;

    companion object {
        val logger = LoggerFactory.getLogger("mke-utils")

        val value by lazy {
            try {
                val value = PropertiesFactory.from("env.properties").getOrNull("value")?.uppercase()
                if (value == null) {
                    logger.warn("'value' property is not set in env.properties, using DEV environment")
                    DEV
                } else {
                    valueOf(value)
                }
            } catch (_: FileNotFoundException) {
                logger.warn("env.properties file not found, using DEV environment")
                DEV
            } catch (e: Exception) {
                throw e
            }
        }
        fun isDev() = value != PROD
    }
}

