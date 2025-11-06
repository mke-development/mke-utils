package team.mke.utils.ktor.openapi

import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.config.SchemaOverwriteModule
import io.swagger.v3.oas.models.media.Schema

fun SchemaGenerator.TypeOverwrites.LocalDate(schema: Schema<Any>.() -> Unit = {}) = SchemaOverwriteModule(
    identifier = java.time.LocalDate::class.qualifiedName!!,
    schema = {
        Schema<Any>().apply {
            types = setOf("string")
            format = "yyyy-MM-dd"
            schema()
        }
    },
)

fun SchemaGenerator.TypeOverwrites.ZonedDateTime(schema: Schema<Any>.() -> Unit = {}) = SchemaOverwriteModule(
    identifier = java.time.ZonedDateTime::class.qualifiedName!!,
    schema = {
        Schema<Any>().apply {
            types = setOf("string")
            format = "yyyy-MM-ddTHH:mm:ss+hh:mm"
            schema()
        }
    },
)

fun SchemaGenerator.TypeOverwrites.LocalTime(schema: Schema<Any>.() -> Unit = {}) = SchemaOverwriteModule(
    identifier = java.time.LocalTime::class.qualifiedName!!,
    schema = {
        Schema<Any>().apply {
            types = setOf("string")
            format = "HH:mm"
            schema()
        }
    },
)

fun SchemaGenerator.TypeOverwrites.BigDecimal(schema: Schema<Any>.() -> Unit = {}) = SchemaOverwriteModule(
    identifier = java.math.BigDecimal::class.qualifiedName!!,
    schema = {
        Schema<Any>().apply {
            types = setOf("number")
            format = "#.########"
            schema()
        }
    },
)

fun SchemaGenerator.TypeOverwrites.Duration(schema: Schema<Any>.() -> Unit = {}) = SchemaOverwriteModule(
    identifier = kotlin.time.Duration::class.qualifiedName!!,
    schema = {
        Schema<Any>().apply {
            types = setOf("string")
            format = "Duration ISO-8601"
            schema()
        }
    },
)