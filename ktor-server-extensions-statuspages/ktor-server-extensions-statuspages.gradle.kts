plugins {
    `convention-kmp`
    `convention-tests`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.db)
                implementation(projects.locale)
                implementation(projects.ktorServerExtensions)

                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.status.pages)
                implementation(libs.ktor.server.i18n)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.model)
                implementation(projects.json)
                implementation(libs.kotest)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.server.contentNegotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
    }
}
