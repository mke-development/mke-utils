package team.mke.utils.ktor.server.ext.validator

import io.ktor.http.content.PartData
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Job
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import team.mke.utils.io.validator.FilesValidator
import team.mke.utils.io.validator.defaultTika
import java.io.BufferedInputStream

fun PartData.FileItem.validator(parent: Job? = null, tika: Tika = defaultTika, metadata: Metadata = Metadata()) =
    FilesValidator(tika, BufferedInputStream(provider().toInputStream(parent)), metadata)
