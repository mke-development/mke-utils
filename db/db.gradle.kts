plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            languageSettings {
                enableLanguageFeature("ContextReceivers")
            }

            dependencies {
                api(projects.common)
                implementation(projects.env)

                api(libs.exposed.core)
                api(libs.exposed.dao)
                api(libs.exposed.java.time)
                api(libs.exposed.jdbc)
                api(libs.exposed.migration)
                api(libs.hikari)

                api(libs.raysmith.exposedOption)
                implementation(libs.reflections)
                implementation(libs.raysmith.utils)
            }
        }
    }
}