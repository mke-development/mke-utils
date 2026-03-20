package team.mke.utils.model

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Name
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

// TODO move to ktor-server-options module
/**
 * Представляет значение опции
 *
 * @property value Значение
 * */
@Description("Представляет значение опции")
@Serializable
@Name("OptionValue")
data class OptionValue<out T : Any?>(

    @Description("Значение")
    @Contextual val value: T?
)
