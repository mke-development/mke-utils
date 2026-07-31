plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.model)
            }
        }

        jvmMain {
            dependencies {
                api(projects.ktorOpenapi)
                api(projects.crashInterceptor)
                api(projects.json)
                api(projects.common)

                api(libs.ktor.server.core)
                api(libs.raysmith.exposedOption)
                api(libs.kenerator.core)
                implementation(libs.raysmith.utils)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.testDb)
                implementation(projects.db)
                implementation(libs.kotest)
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.server.contentNegotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
    }
}
