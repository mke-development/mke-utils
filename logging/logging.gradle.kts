plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.slf4j.api)
            }
        }
    }
}