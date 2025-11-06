plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(projects.json)

                api(libs.ktor.client.contentNegotiation)
                api(libs.ktor.serialization.kotlinx.json)
            }
        }
    }
}