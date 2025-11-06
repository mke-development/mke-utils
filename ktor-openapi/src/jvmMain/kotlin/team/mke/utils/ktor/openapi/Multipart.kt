package team.mke.utils.ktor.openapi

import io.github.smiley4.ktoropenapi.config.MultipartBodyConfig
import io.github.smiley4.ktoropenapi.config.SchemaOverwriteModule
import io.github.smiley4.schemakenerator.core.annotations.*
import io.ktor.http.*
import io.swagger.v3.oas.models.media.Schema
import team.mke.utils.serialization.getSerialName
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.javaType


@OptIn(ExperimentalStdlibApi::class)
fun MultipartBodyConfig.apply(clazz: KClass<*>, openApiOverwrites: List<SchemaOverwriteModule> = emptyList()) {
    clazz.members
        .filter { it.hasAnnotation<Description>() }
        .forEach {
            val schema = Schema<Any>().apply {
                val name = (it.returnType.classifier as? KClass<*>)?.findAnnotation<Name>()?.name
                val isObject = name != null

                types = it.type()
                format = it.findAnnotation<Format>()?.format ?: if (isObject) {
                    name
                } else {
                    openApiOverwrites.find { overwrite ->
                        overwrite.identifier == it.returnType.javaType.typeName
                    }?.schema?.invoke()?.format
                }
                description = it.findAnnotation<Description>()?.description
                deprecated = it.hasAnnotation<Deprecated>()
                example = it.findAnnotation<Example>()?.example ?: ""
                nullable = it.returnType.isMarkedNullable

                types = if (isObject) {
                    setOf("object")
                } else {
                    it.type()
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