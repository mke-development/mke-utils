package team.mke.utils.db.json.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import team.mke.utils.db.json.jsonTransformer

class JsonTransformerTests : FreeSpec({
    "jsonTransformer should serialize and deserialize object" {
        val transformer = jsonTransformer<TestData>()
        val data = TestData(42, "hello")

        val serialized = transformer.unwrap(data)
        serialized shouldBe """
            {
                "id": 42,
                "name": "hello"
            }
        """.trimIndent()

        val deserialized = transformer.wrap(serialized)
        deserialized shouldBe data
    }

    "jsonTransformer with custom Json configuration" {
        val customJson = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }
        val transformer = jsonTransformer<TestData>(customJson)

        val jsonStringWithExtra = """
            {
                "id":100,
                "name":"extra",
                "unknown":"field"
            }
        """.trimIndent()
        val deserialized = transformer.wrap(jsonStringWithExtra)
        deserialized shouldBe TestData(100, "extra")
    }
}) {
    companion object {
        @Serializable
        data class TestData(val id: Int, val name: String)
    }
}
