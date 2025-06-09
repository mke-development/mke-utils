plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(projects.env)

                api(libs.ktor.client.core)
                api(libs.slf4j.api)
                api(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
            }
        }
    }
}