plugins {
    `convention-kmp`
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.ktorClient)
                implementation(projects.env)

                api(libs.slf4j.api)
                api(libs.ktor.http)
                implementation(libs.raysmith.utils)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
    }
}