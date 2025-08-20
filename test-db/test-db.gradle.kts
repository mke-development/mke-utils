plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    sourceSets {
        jvmMain {
            languageSettings {
                enableLanguageFeature("ContextReceivers")
                enableLanguageFeature("WhenGuards")
            }
            dependencies {
                implementation(projects.db)
                implementation(projects.io)

                implementation(libs.exposed.core)
                implementation(libs.exposed.dao)
                implementation(libs.kotest)
                implementation(libs.kotest.extensions.htmlreporter)
                implementation(libs.kotest.extensions.junitxml)
                implementation(libs.kotest.html.reporter)
                implementation(libs.h2)
                implementation(libs.reflections)
            }
        }
    }
}