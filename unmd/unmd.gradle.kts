plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.markdown)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }
    }
}