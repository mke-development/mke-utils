package team.mke.utils.db.json

import kotlinx.serialization.json.Json
import ru.raysmith.exposedoption.Transformer
import ru.raysmith.exposedoption.transformer

/**
 * Создает [Transformer] для сериализации и десериализации объекта типа [Wrapped] в JSON-строку
 * с использованием [kotlinx.serialization.json.Json].
 *
 * @param json экземпляр [Json] для сериализации/десериализации. По умолчанию [team.mke.utils.json.json].
 */
inline fun <reified Wrapped> jsonTransformer(json: Json = team.mke.utils.json.json): Transformer<String, Wrapped> =
    transformer(
        { json.encodeToString(it) },
        { json.decodeFromString(it) }
    )
