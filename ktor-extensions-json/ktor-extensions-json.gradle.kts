plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(projects.json)

                api(libs.ktor.http)
                implementation(libs.ktor.server.core)
            }
        }
    }
}