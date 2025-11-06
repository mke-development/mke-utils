package team.mke.utils.ktor.openapi

import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.ResponsesConfig
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.config.SchemaConfig
import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.annotations.Type
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.serializerOrNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.javaType
import kotlin.reflect.jvm.jvmName

typealias OpenApiRouteBlock = RouteConfig.() -> Unit

fun ResponsesConfig.ok(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.OK to {
    description = "Успех"
    block()
}

fun ResponsesConfig.created(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.Created to {
    description = "Запись создана"
    block()
}

fun ResponsesConfig.badRequest(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.BadRequest to {
    description = "Неверные данные"
    block()
}
fun ResponsesConfig.notFound(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.NotFound to {
    description = "Сущность не найдена"
    block()
}
fun ResponsesConfig.locked(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.Locked to {
    description = "Изменение заблокировано другим запросом"
    block()
}
fun ResponsesConfig.forbidden(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.Forbidden to {
    description = "Отсутствуют необходимые права"
    block()
}
fun ResponsesConfig.noContent(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.NoContent to {
    description = "Нет данных для ответа"
    block()
}
fun ResponsesConfig.unauthorized(block: ResponseConfig.() -> Unit = {}) = HttpStatusCode.Unauthorized to {
    description = "Авторизация не пройдена"
    block()
}

val KClass<*>.documentationName get() = findAnnotation<Name>()?.name
    ?: serializerOrNull(starProjectedType)?.descriptor?.serialName
    ?: simpleName ?: jvmName

fun SchemaConfig.schema(type: KClass<*>) = schema(
    schemaId = type.documentationName,
    schema = type.starProjectedType
)

@OptIn(ExperimentalStdlibApi::class)
fun KCallable<*>.type() = findAnnotation<Type>()?.type?.let { setOf(it) } ?: when(returnType.javaType) {
    String::class.java -> setOf("string")
    Int::class.java, Long::class.java, BigDecimal::class.java, Double::class.java, Float::class.java -> setOf("number")
    LocalDate::class.java, ZonedDateTime::class.java, LocalDateTime::class.java -> setOf("string")
    Boolean::class.java -> setOf("boolean")
    else -> setOf("json")
}