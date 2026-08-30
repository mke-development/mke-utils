package team.mke.utils.ktor.server.ext.statuspages

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import team.mke.utils.db.EntityNotFoundException
import team.mke.utils.db.Gender
import team.mke.utils.db.I18nEntityName
import team.mke.utils.model.ErrorDTO

object TestTable : IntIdTable("test_table")

@I18nEntityName("ru", "Пользователь", Gender.MASCULINE)
@I18nEntityName("en", "User", Gender.MASCULINE)
class TestUser(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TestUser>(TestTable)
}

@I18nEntityName("ru", "Статья", Gender.FEMININE)
@I18nEntityName("en", "Article", Gender.FEMININE)
class TestArticle(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TestArticle>(TestTable)
}

class StatusPagesExtTest : FunSpec({

    test("should format English message when Accept-Language is en") {
        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
            }
            application {
                install(ServerContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
                install(StatusPages) {
                    handleEntityNotFoundException()
                }
                routing {
                    get("/test") {
                        throw EntityNotFoundException(TestUser.Companion, 42)
                    }
                }
            }

            val response = client.get("/test") {
                header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
            }

            response.status shouldBe HttpStatusCode.NotFound
            val error = response.body<ErrorDTO>()
            error.message shouldBe "User with id 42 not found"
            error.path shouldBe "/test"
        }
    }

    test("should format Russian message with masculine gender when Accept-Language is ru") {
        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
            }
            application {
                install(ServerContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
                install(StatusPages) {
                    handleEntityNotFoundException()
                }
                routing {
                    get("/test") {
                        throw EntityNotFoundException(TestUser.Companion, 42)
                    }
                }
            }

            val response = client.get("/test") {
                header(HttpHeaders.AcceptLanguage, "ru-RU,ru;q=0.9")
            }

            response.status shouldBe HttpStatusCode.NotFound
            val error = response.body<ErrorDTO>()
            error.message shouldBe "Пользователь с id 42 не найден"
            error.path shouldBe "/test"
        }
    }

    test("should format Russian message with feminine gender when Accept-Language is ru") {
        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
            }
            application {
                install(ServerContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
                install(StatusPages) {
                    handleEntityNotFoundException()
                }
                routing {
                    get("/test") {
                        throw EntityNotFoundException(TestArticle.Companion, 10)
                    }
                }
            }

            val response = client.get("/test") {
                header(HttpHeaders.AcceptLanguage, "ru")
            }

            response.status shouldBe HttpStatusCode.NotFound
            val error = response.body<ErrorDTO>()
            error.message shouldBe "Статья с id 10 не найдена"
            error.path shouldBe "/test"
        }
    }

    test("should use messageSupplier first when it returns non-null string") {
        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
            }
            application {
                install(ServerContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
                install(StatusPages) {
                    handleEntityNotFoundException { call, e ->
                        if (call.locale.language == "de") "Nicht gefunden (id ${e.id})" else null
                    }
                }
                routing {
                    get("/test") {
                        throw EntityNotFoundException(TestUser.Companion, 99)
                    }
                }
            }

            val responseDe = client.get("/test") {
                header(HttpHeaders.AcceptLanguage, "de-DE,de;q=0.9")
            }
            responseDe.status shouldBe HttpStatusCode.NotFound
            responseDe.body<ErrorDTO>().message shouldBe "Nicht gefunden (id 99)"

            val responseEn = client.get("/test") {
                header(HttpHeaders.AcceptLanguage, "en")
            }
            responseEn.status shouldBe HttpStatusCode.NotFound
            responseEn.body<ErrorDTO>().message shouldBe "User with id 99 not found"
        }
    }

    test("should fallback to Russian when messageSupplier returns null for unsupported language") {
        testApplication {
            val client = createClient {
                install(ClientContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
            }
            application {
                install(ServerContentNegotiation) {
                    json(team.mke.utils.json.json)
                }
                install(StatusPages) {
                    handleEntityNotFoundException { _, _ -> null }
                }
                routing {
                    get("/test") {
                        throw EntityNotFoundException(TestUser.Companion, 99)
                    }
                }
            }

            val response = client.get("/test") {
                header(HttpHeaders.AcceptLanguage, "es")
            }

            response.status shouldBe HttpStatusCode.NotFound
            val error = response.body<ErrorDTO>()
            error.message shouldBe "Пользователь с id 99 не найден"
            error.path shouldBe "/test"
        }
    }
})
