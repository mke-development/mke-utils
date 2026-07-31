package team.mke.utils.ktor.server.ext.validator

import io.ktor.http.ContentType
import io.ktor.http.content.*
import io.ktor.utils.io.jvm.javaio.*
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import team.mke.utils.io.validator.FilesValidator
import team.mke.utils.io.validator.defaultTika
import java.io.BufferedInputStream

fun PartData.FileItem.validator(tika: Tika = defaultTika) =
    FilesValidator(tika, BufferedInputStream(provider().toInputStream()))

fun PartData.FileItem.metadata(fileNameFallback: String) = Metadata().apply {
    set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFileName ?: fileNameFallback)
}

fun PartData.FileItem.metadata() = Metadata().apply {
    set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFileName)
}

fun FilesValidator.checkMimeType(vararg allow: ContentType, errorMessage: () -> String) =
    checkMimeType(*allow.map { it.toString() }.toTypedArray(), errorMessage = errorMessage)
