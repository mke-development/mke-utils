plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    setupJvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kenerator.core)
            }
        }

        jvmMain {
            dependencies {
                api(libs.ktor.http)
                implementation(libs.raysmith.utils)

            }
        }
    }
}