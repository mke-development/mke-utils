package team.mke.utils.ktor.server.options.tests

import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import ru.raysmith.exposedoption.Options
import ru.raysmith.exposedoption.getOrNull
import ru.raysmith.exposedoption.option
import team.mke.utils.crashinterceptor.impl.TestCrashInterceptor
import team.mke.utils.db.BaseDatabase
import team.mke.utils.ktor.server.options.OptionMethodImpl
import team.mke.utils.ktor.server.options.OptionsPluginConfiguration
import team.mke.utils.ktor.server.options.OptionsRoutingContext
import team.mke.utils.ktor.server.options.configureOptions
import team.mke.utils.model.OptionValue
import team.mke.utils.test.db.DatabaseTest
import kotlin.time.Duration

object Database : BaseDatabase() {
    context(tr: JdbcTransaction)
    override fun migration(connection: Database, toVersion: Int) {

    }
}

private var foo by option<String?>("foo", Duration.INFINITE) { getOrNull() }
private var bar by option<String?>("bar", Duration.INFINITE) { getOrNull() }

class OptionsTests : DatabaseTest(Database::class, listOf(Options), {

    fun testOptionsApplication(
        setup: suspend context(OptionsPluginConfiguration, Route) OptionsRoutingContext.() -> Unit,
        test: suspend (HttpClient) -> Unit
    ) = testApplication {
        client = createClient {
            this.install(ContentNegotiation) {
                json(team.mke.utils.json.json)
            }
        }

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(team.mke.utils.json.json)
            }

            routing {
                configureOptions {
                    crashInterceptor = TestCrashInterceptor
                    routes {
                        runBlocking {
                            setup()
                        }
                    }
                }
            }
        }

        test(this@testApplication.client)
    }

    val docs = object : OptionMethodImpl() { }.apply {
        generate<String>("stub", "stub" to "stub", "stub" to "stub")
    }

    "[GET] /options/*" {
        foo = "foo"

        testOptionsApplication({
            setup<String?>(::foo, docs)
            setup<String?>(::bar, docs)
        }) { client ->
            client.get("options/foo").body<OptionValue<String?>>().value shouldBe "foo"
            client.get("options/bar").body<OptionValue<String?>>().value.shouldBeNull()
        }
    }

    "[PUT] /options/*" {
        foo = null

        testOptionsApplication({
            setup<String?>(::foo, docs)
        }) { client ->
            client.put("options/foo") {
                setBody("foo")
            }

            foo shouldBe "foo"
        }
    }

    "[GET] /options" {
        foo = "foo"
        bar = null

        testOptionsApplication({
            setup<String?>(::foo, docs)
            setup<String?>(::bar, docs)
        }) { client ->
            client.get("/options") {
                url {
                    parameters.appendAll("keys", listOf("foo", "bar"))
                }
            }.body<Map<String, OptionValue<String?>>>()
                .shouldHaveSize(2)
        }
    }

    "[PUT] /options" {
        foo = "foo"
        bar = null

        testOptionsApplication({
            setup<String?>(::foo, docs)
            setup<String?>(::bar, docs)
        }) { client ->
            client.put("/options") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("foo" to "upd", "bar" to "bar"))
            }

            foo shouldBe "upd"
            bar shouldBe "bar"
        }
    }

    "[POST] /test" {
        foo = "foo"

        testOptionsApplication({
            setup<String?>(::foo, docs, test = { if (it != foo) error("test fail") })
        }) { client ->
            client.post("/options/foo/test") { setBody("foo") }.status.shouldBe(HttpStatusCode.OK)
            client.post("/options/foo/test") { setBody("wrong") }.status.shouldBe(HttpStatusCode.InternalServerError)
        }
    }

    "onUpdate" {
        var onUpdateCalled = 0
        foo = "foo"
        bar = "bar"

        val onUpdate: suspend (String?) -> Unit = { _: String? -> onUpdateCalled += 1 }

        testOptionsApplication({
            setup<String?>(::foo, docs, onUpdate = onUpdate)
            setup<String?>(::bar, docs, onUpdate = onUpdate)
        }) { client ->
            client.put("/options") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("foo" to "upd", "bar" to "bar"))
            }

            onUpdateCalled shouldBe 1

            client.put("/options/foo") {
                setBody("upd2")
            }

            onUpdateCalled shouldBe 2

            client.put("/options/foo") {
                setBody("upd2")
            }

            onUpdateCalled shouldBe 2

            client.put("/options") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("foo" to "upd3", "bar" to "upd3"))
            }

            onUpdateCalled shouldBe 3
        }
    }

    "verification" {
        foo = "foo"

        testOptionsApplication({
            setup<String?>(::foo, docs, verification = { require(it == "upd") { "foo should be upd" } })
        }) { client ->
            client.put("/options") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("foo" to "wrong"))
            }.status.shouldBe(HttpStatusCode.InternalServerError)
            foo shouldBe "foo"

            client.put("/options") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("foo" to "upd"))
            }.status.shouldBe(HttpStatusCode.OK)
            foo shouldBe "upd"
        }
    }
})
