plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(libs.ktor.server.core)
                api(libs.exposed.core)
                api(libs.exposed.jdbc)
            }
        }
    }
}