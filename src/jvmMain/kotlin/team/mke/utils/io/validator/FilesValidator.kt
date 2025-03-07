package team.mke.utils.io.validator

import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MimeTypes
import java.io.BufferedInputStream
import java.io.InputStream

open class FilesValidator(val stream: BufferedInputStream) : AutoCloseable {

    val mimeType by lazy { MimeTypes.getDefaultMimeTypes().detect(stream, Metadata()) }

    companion object {
        fun instance(stream: InputStream) = FilesValidator(BufferedInputStream(stream))
    }

    fun checkMimeTypeBaseType(vararg allow: String, errorMessage: () -> String) = also {
        require(mimeType.baseType.type in allow, errorMessage)
    }

    fun checkMimeType(vararg allow: String, errorMessage: () -> String) = also {
        require(mimeType.toString() in allow, errorMessage)
    }

    override fun close() {
        stream.close()
    }
}