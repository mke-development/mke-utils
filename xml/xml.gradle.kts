plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(projects.env)
                api(libs.xmlutil.serialization)
            }
        }
    }
}