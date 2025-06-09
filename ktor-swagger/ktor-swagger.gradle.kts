plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(libs.ktor.swaggerUi)
                implementation(libs.ktor.http)
            }
        }
    }
}