package team.mke.utils.io.validator

import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Job
import java.io.BufferedInputStream
import java.io.InputStream

fun InputStream.validator() = FilesValidator(BufferedInputStream(this))
fun ByteReadChannel.validator(parent: Job? = null) = FilesValidator(BufferedInputStream(this.toInputStream(parent)))
fun FilesValidator.toImageValidator() = ImageValidator(stream)
fun <T : FilesValidator, R> T.useStream(block: T.(BufferedInputStream) -> R): R {
    return use {
        block(this, stream)
    }
}