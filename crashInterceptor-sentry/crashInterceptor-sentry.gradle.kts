plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            dependencies {
                api(projects.env)
                api(projects.crashInterceptor)

                api(libs.sentry)
                api(libs.slf4j.api)
            }
        }
    }
}