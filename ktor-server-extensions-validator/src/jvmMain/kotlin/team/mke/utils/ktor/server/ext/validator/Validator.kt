package team.mke.utils.ktor.server.ext.validator

import io.ktor.http.content.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Job
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import team.mke.utils.io.validator.FilesValidator
import team.mke.utils.io.validator.defaultTika
import java.io.BufferedInputStream

fun PartData.FileItem.validator(parent: Job? = null, tika: Tika = defaultTika, metadata: Metadata = Metadata()) =
    FilesValidator(tika, BufferedInputStream(provider().toInputStream(parent)), metadata)

fun PartData.FileItem.metadata(fileNameFallback: String) = Metadata().apply {
    set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFileName ?: fileNameFallback)
}
