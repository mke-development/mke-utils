package team.mke.utils.io.validator

import org.apache.commons.imaging.ImageInfo
import org.apache.commons.imaging.Imaging
import org.apache.tika.Tika
import org.apache.tika.mime.MediaType
import java.io.BufferedInputStream

class ImageValidator(
    tika: Tika,
    stream: BufferedInputStream,
    contentType: MediaType? = null,
    fileShouldBeImageErrorMessage: String = "Файл должен быть изображением",
) : FilesValidator(tika, stream, contentType) {

    val ext: String by lazy { super.mediaType.subtype }
    val imageData: ImageInfo by lazy {
        stream.mark(Int.MAX_VALUE)
        val imageData = Imaging.getImageInfo(stream, "file.$ext")
        stream.reset()
        imageData
    }

    init {
        checkMimeTypeBaseType("image") { fileShouldBeImageErrorMessage }
    }

    fun checkImage(compare: (imageData: ImageInfo) -> Boolean, errorMessage: () -> String) = also {
        require(compare(imageData)) {
            errorMessage()
        }
    }

    fun checkSize(compare: (w: Int, h: Int) -> Boolean, errorMessage: () -> String) = also {
        require(compare(imageData.width, imageData.height)) {
            logger.debug("Image size: ${imageData.width}x${imageData.height}")
            errorMessage()
        }
    }

    fun checkWidth(compare: (w: Int) -> Boolean, errorMessage: () -> String) = also {
        require(compare(imageData.width)) {
            logger.debug("Image width: ${imageData.width}")
            errorMessage()
        }
    }

    fun checkHeight(compare: (h: Int) -> Boolean, errorMessage: () -> String) = also {
        require(compare(imageData.height)) {
            logger.debug("Image height: ${imageData.height}")
            errorMessage()
        }
    }
}
