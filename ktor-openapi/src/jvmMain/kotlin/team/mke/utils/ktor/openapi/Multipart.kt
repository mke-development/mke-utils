package team.mke.utils.ktor.openapi

import io.github.smiley4.ktoropenapi.config.MultipartBodyConfig
import io.github.smiley4.ktoropenapi.config.SchemaOverwriteModule
import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.annotations.Optional
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.ktor.http.*
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.Transient
import team.mke.utils.serialization.getSerialName
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.javaType

@OptIn(ExperimentalStdlibApi::class)
context(kc: KCallable<*>)
private fun List<SchemaOverwriteModule>.findSchema() = find { overwrite ->
    overwrite.identifier == kc.returnType.javaType.typeName
}?.schema?.invoke()

private fun KCallable<*>.format(openApiOverwrites: List<SchemaOverwriteModule>) = findAnnotation<Format>()?.format
    ?: findAnnotation<io.swagger.v3.oas.annotations.media.Schema>()?.format
    ?: (returnType.classifier as? KClass<*>)?.findAnnotation<Name>()?.name ?: openApiOverwrites.findSchema()?.format

@OptIn(ExperimentalStdlibApi::class)
fun MultipartBodyConfig.apply(clazz: KClass<*>, openApiOverwrites: List<SchemaOverwriteModule> = emptyList()) {
    clazz.members
        .filter {
            it.hasAnnotation<Description>() &&
            it.findAnnotation<io.swagger.v3.oas.annotations.media.Schema>()?.hidden != true &&
                !it.hasAnnotation<Hidden>() &&
                !it.hasAnnotation<Transient>()
        }
        .forEach {
            val schema = Schema<Any>().apply {
                name = (it.returnType.classifier as? KClass<*>)?.findAnnotation<Name>()?.name
                types = it.type()
                format = it.format(openApiOverwrites)
                description = it.findAnnotation<Description>()?.description
                deprecated = it.hasAnnotation<Deprecated>()
                it.findAnnotation<Example>()?.example?.let { value ->
                    example = value
                }
                nullable = it.returnType.isMarkedNullable
                enum = it.findAnnotation<io.swagger.v3.oas.annotations.media.Schema>()?.allowableValues?.toList()
                it.findAnnotation<Default>()?.value
                    ?: it.findAnnotation<io.swagger.v3.oas.annotations.media.Schema>()?.defaultValue?.let { value ->
                        default = value
                    }

            }

            part(it.getSerialName(), schema) {
                required = (!it.returnType.isMarkedNullable || it.hasAnnotation<Required>()) && !it.hasAnnotation<Optional>()
                mediaTypes = when {
                    it.returnType.javaType == String::class.java -> setOf(ContentType.Text.Plain)
                    it.returnType.javaType == Int::class.java -> setOf(ContentType.Text.Plain)
                    type.javaClass.isEnum -> setOf(ContentType.Text.Plain) // TODO not work
                    else -> setOf(ContentType.Application.Json)
                }
            }
        }
}
