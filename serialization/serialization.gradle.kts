plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(projects.crashInterceptor)

                api(libs.slf4j.api)
                api(libs.kotlinx.serialization.json)
                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.datetime)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.common)
                implementation(projects.crashInterceptor)

                implementation(libs.kotest)
                implementation(libs.raysmith.utils)
            }
        }
    }
}