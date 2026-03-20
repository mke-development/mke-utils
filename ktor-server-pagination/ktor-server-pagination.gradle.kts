plugins {
    `convention-kmp`
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.locale)
                implementation(projects.json)
                implementation(projects.ktorExtensionsJson)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.openapi.tools.openapi)
                implementation(libs.swagger.models)
                implementation(libs.raysmith.utils)
            }
        }
    }
}