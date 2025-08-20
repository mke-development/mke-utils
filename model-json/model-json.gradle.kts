plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    setupJvm()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.model)

                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}