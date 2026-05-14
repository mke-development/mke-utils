package team.mke.utils.model

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Name
import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO move to ktor-server-options module
/**
 * Представляет значение опции
 *
 * @property value Значение
 * */
@Description("Представляет значение опции")
@JsonSchema.Description("Представляет значение опции")
@Serializable
@Name("OptionValue")
@SerialName("OptionValue")
data class OptionValue<out T : Any?>(

    @Description("Значение")
    @JsonSchema.Description("Значение")
    @Contextual val value: T?
)
