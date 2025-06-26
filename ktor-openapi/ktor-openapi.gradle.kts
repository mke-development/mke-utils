plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(libs.ktor.openapi.tools.openapi)
                implementation(libs.ktor.http)
            }
        }
    }
}