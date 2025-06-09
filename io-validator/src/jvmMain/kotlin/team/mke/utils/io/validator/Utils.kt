package team.mke.utils.io.validator

import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import java.io.BufferedInputStream
import java.io.InputStream

val defaultTika = Tika()

fun InputStream.validator(tika: Tika = defaultTika, metadata: Metadata = Metadata()) =
    FilesValidator(tika, BufferedInputStream(this), metadata)
fun FilesValidator.toImageValidator(tika: Tika = defaultTika) = ImageValidator(tika, stream, metadata = metadata)
fun <T : FilesValidator, R> T.useStream(block: T.(BufferedInputStream) -> R): R {
    return use {
        block(this, stream)
    }
}