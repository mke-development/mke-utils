plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.server)
            }
        }
        jvmMain {
            languageSettings {
                enableLanguageFeature("ContextReceivers")
            }

            dependencies {
                api(projects.ktorSwagger)
                api(projects.crashInterceptor)
                api(projects.json)
                api(projects.common)

                api(libs.ktor.server.core)
                api(libs.raysmith.exposedOption)
                implementation(libs.raysmith.utils)
            }
        }
    }
}