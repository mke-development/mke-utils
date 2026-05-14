plugins {
    `convention-kmp`
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.swagger.annotations)
                implementation(libs.raysmith.utils)
                implementation(libs.kenerator.core)
                implementation(libs.ktor.server.openapi)
                api(libs.ktor.http)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.kotlin.reflect)
            }
        }
    }
}
