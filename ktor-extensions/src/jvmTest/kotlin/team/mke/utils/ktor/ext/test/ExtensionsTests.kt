package team.mke.utils.ktor.ext.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import team.mke.utils.ktor.ext.prepareFile
import java.io.File

class ExtensionsTests : FreeSpec({

    val partData = PartData.FileItem(
        provider = { ByteReadChannel.Empty },
        dispose = {},
        partHeaders = Headers.build {
            append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"file.txt\"")
        }
    )

    "prepareFile should return correct path with same filename for not-existed file" {
        partData.prepareFile("home").path.replace("\\", "/") shouldBe "home/file.txt"
    }

    "prepareFile should return correct path with different filename for existed file" {
        val file = File("home/file.txt")
        file.mkdirs()
        file.createNewFile()
        file.deleteOnExit()
        partData.prepareFile("home").path.replace("\\", "/") shouldMatch "home/file_[a-f0-9-]{36}\\.txt"
    }

})