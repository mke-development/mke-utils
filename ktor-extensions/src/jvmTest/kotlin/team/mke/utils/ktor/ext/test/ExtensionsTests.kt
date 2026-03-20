package team.mke.utils.ktor.ext.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import ru.raysmith.utils.uuid
import team.mke.utils.ktor.ext.prepareFile

class ExtensionsTests : FreeSpec({
    val newName = uuid()

    fun partData(filename: String) = PartData.FileItem(
        provider = { ByteReadChannel.Empty },
        dispose = {},
        partHeaders = Headers.build {
            append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$filename\"")
        }
    )

    "prepareFile should return correct path with provided nextName" {
        val partData = partData("file.txt")
        partData.prepareFile("home") { newName }.path shouldBe "home\\$newName.txt"
    }

    "prepareFile should use fallback extension when original filename has no extension" {
        val partData = partData("file")
        partData.prepareFile("home", "unk") { newName }.path shouldBe "home\\$newName.unk"
    }

    "prepareFile should return correct filename when original filename has no extension and no fallback is provided" {
        val partData = partData("file")
        partData.prepareFile("home") { newName }.path shouldBe "home\\$newName"
    }
})