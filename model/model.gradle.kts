plugins {
    `convention-kmp`
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.swagger.annotations)
                implementation(libs.raysmith.utils)
                implementation(libs.kenerator.core)
                api(libs.ktor.http)
            }
        }
    }
}