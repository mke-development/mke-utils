plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(projects.env)
                implementation(projects.serialization)

                api(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}