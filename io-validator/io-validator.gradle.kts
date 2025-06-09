plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(libs.tika.core)
                implementation(libs.apache.commons.imaging)
            }
        }
    }
}