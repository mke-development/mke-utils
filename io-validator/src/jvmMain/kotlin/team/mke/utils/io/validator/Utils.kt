package team.mke.utils.io.validator

import org.apache.tika.Tika
import java.io.BufferedInputStream
import java.io.InputStream

val defaultTika = Tika()

fun InputStream.validator(tika: Tika = defaultTika) =
    FilesValidator(tika, BufferedInputStream(this))
fun FilesValidator.toImageValidator(tika: Tika = defaultTika) = ImageValidator(tika, stream, mediaType)
suspend fun <T : FilesValidator, R> T.useStream(block: suspend T.(BufferedInputStream) -> R): R {
    return use {
        block(this, stream)
    }
}
