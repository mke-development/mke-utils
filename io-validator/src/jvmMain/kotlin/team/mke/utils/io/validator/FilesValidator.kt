package team.mke.utils.io.validator

import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.InputStream

open class FilesValidator(
    tika: Tika,
    val stream: BufferedInputStream,
    detectedContentType: MediaType? = null,
) : AutoCloseable {

    private var metadata: Metadata = Metadata()

    val mediaType by lazy {
        detectedContentType ?: run {
            tika.detect(stream, metadata).let {
                val baseType = it.substringBefore('/')
                val subType = it.substringAfter('/')
                MediaType(baseType, subType)
            }
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger("validator")
        fun instance(tika: Tika, stream: InputStream, detectedContentType: MediaType? = null) =
            FilesValidator(tika, stream.buffered(), detectedContentType)
    }

    fun checkMimeTypeBaseType(vararg allow: String, errorMessage: () -> String): FilesValidator {
        require(mediaType.baseType.type in allow) {
            logger.debug("Content MIME base type: {}, allowed: {}", mediaType.baseType.type, allow.joinToString())
            errorMessage()
        }
        return this
    }

    fun checkMimeType(vararg allow: String, errorMessage: () -> String): FilesValidator {
        require(mediaType.toString() in allow) {
            logger.debug("Content MIME type: {}, allowed: {}", mediaType, allow.joinToString())
            errorMessage()
        }
        return this
    }

    fun withMetadata(metadata: Metadata): FilesValidator {
        this.metadata = metadata
        return this
    }

    override fun close() {
        stream.close()
    }
}
