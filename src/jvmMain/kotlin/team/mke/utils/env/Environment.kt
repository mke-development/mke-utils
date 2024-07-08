package team.mke.utils.env

import ru.raysmith.utils.properties.PropertiesFactory
import ru.raysmith.utils.properties.getOrNull

/** Значение из env.properties */
val env by lazy { Environment.value }

/** Значение из env.properties */
enum class Environment {
    DEV, PROD;

    companion object {
        val value by lazy {
            valueOf(
                PropertiesFactory.from("env.properties").getOrNull("value")?.uppercase()
                    ?: error("Не найдено значение 'value' в env.properties")
            )
        }
        fun isDev() = value != PROD
    }
}

