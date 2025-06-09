package team.mke.utils.io.validator

import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.apache.tika.mime.MimeTypes
import java.io.BufferedInputStream
import java.io.InputStream

open class FilesValidator(val tika: Tika, val stream: BufferedInputStream, val metadata: Metadata = Metadata()) : AutoCloseable {

    val signatureMimeType: MediaType by lazy { MimeTypes.getDefaultMimeTypes().detect(stream, metadata) }

    val contentMimeType: MediaType by lazy { tika.detect(stream, metadata).let {
        val baseType = it.substringBefore('/')
        val subType = it.substringAfter('/')
        MediaType(baseType, subType)
    } }

    companion object {
        fun instance(tika: Tika, stream: InputStream) = FilesValidator(tika, stream.buffered())
    }

    fun checkSignatureMimeTypeBaseType(vararg allow: String, errorMessage: () -> String) = also {
        require(signatureMimeType.baseType.type in allow, errorMessage)
    }

    fun checkSignatureMimeType(vararg allow: String, errorMessage: () -> String) = also {
        require(signatureMimeType.toString() in allow, errorMessage)
    }

    fun checkContentMimeTypeBaseType(vararg allow: String, errorMessage: () -> String) = also {
        require(contentMimeType.baseType.type in allow, errorMessage)
    }

    fun checkContentMimeType(vararg allow: String, errorMessage: () -> String) = also {
        require(contentMimeType.toString() in allow, errorMessage)
    }

    override fun close() {
        stream.close()
    }
}