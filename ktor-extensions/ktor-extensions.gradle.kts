plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.server)
            }
        }
        jvmMain {
            dependencies {
                api(libs.ktor.server.core)
                api(libs.raysmith.utils)
            }
        }
    }
}