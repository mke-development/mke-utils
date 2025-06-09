package team.mke.utils.ktor.client.ext

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.installContentNegotiationJson(json: Json = team.mke.utils.json.json) {
    install(ContentNegotiation) {
        json(json)
    }
}