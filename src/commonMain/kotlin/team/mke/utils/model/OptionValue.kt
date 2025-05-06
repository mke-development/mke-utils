package team.mke.utils.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/** Представляет значение опции */
@Serializable
data class OptionValue<out T : Any?>(

    /** Значение опции */
    @Contextual val value: T?
)