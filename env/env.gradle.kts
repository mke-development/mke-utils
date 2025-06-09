plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.raysmith.utils)
                implementation(libs.slf4j.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }
    }
}