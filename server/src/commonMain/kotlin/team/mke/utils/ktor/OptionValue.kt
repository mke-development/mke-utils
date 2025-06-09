package team.mke.utils.ktor

import io.github.smiley4.schemakenerator.core.annotations.Description
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Представляет значение опции
 *
 * @property value Значение
 * */
@Serializable
@Description("Представляет значение опции")
data class OptionValue<out T : Any?>(

    @Description("Значение")
    @Contextual val value: T?
)