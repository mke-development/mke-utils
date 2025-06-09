package team.mke.utils.io.validator

import org.apache.commons.imaging.ImageInfo
import org.apache.commons.imaging.Imaging
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import java.io.BufferedInputStream

class ImageValidator(
    tika: Tika,
    stream: BufferedInputStream,
    fileShouldBeImageErrorMessage: String = "Файл должен быть изображением",
    metadata: Metadata = Metadata()
) : FilesValidator(tika, stream, metadata) {

    val ext: String by lazy { signatureMimeType.subtype }
    val imageData: ImageInfo by lazy {
        stream.mark(Int.MAX_VALUE)
        val imageData = Imaging.getImageInfo(stream, "file.$ext")
        stream.reset()
        imageData
    }

    init {
        checkSignatureMimeTypeBaseType("image") { fileShouldBeImageErrorMessage }
    }

    fun checkWidth(compare: (w: Int) -> Boolean, errorMessage: () -> String) = also {
        require(compare(imageData.width), errorMessage)
    }

    fun checkHeight(compare: (h: Int) -> Boolean, errorMessage: () -> String) = also {
        require(compare(imageData.height), errorMessage)
    }
}