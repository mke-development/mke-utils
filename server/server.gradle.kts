plugins {
    `convention-kmp`
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kenerator.core)
            }
        }

        jvmMain {
            dependencies {
                api(libs.ktor.http)
                implementation(libs.raysmith.utils)
            }
        }
    }
}