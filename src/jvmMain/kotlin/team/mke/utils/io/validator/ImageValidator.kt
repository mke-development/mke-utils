package team.mke.utils.io.validator

import org.apache.commons.imaging.ImageInfo
import org.apache.commons.imaging.Imaging
import java.io.BufferedInputStream

class ImageValidator(
    stream: BufferedInputStream,
    fileShouldBeImageErrorMessage: String = "Файл должен быть изображением"
) : FilesValidator(stream) {

    val ext: String by lazy { mimeType.subtype }
    val imageData: ImageInfo by lazy {
        stream.mark(Int.MAX_VALUE)
        val imageData = Imaging.getImageInfo(stream, "file.$ext")
        stream.reset()
        imageData
    }

    init {
        checkMimeTypeBaseType("image") { fileShouldBeImageErrorMessage }
    }

    fun checkWidth(compare: (w: Int) -> Boolean, errorMessage: () -> String) = also {
        require(compare(imageData.width), errorMessage)
    }

    fun checkHeight(compare: (h: Int) -> Boolean, errorMessage: () -> String) = also {
        require(compare(imageData.height), errorMessage)
    }
}