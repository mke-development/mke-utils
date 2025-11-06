plugins {
    `convention-kmp`
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.common)
            }
        }

        jvmMain {
            dependencies {
                api(projects.crashInterceptor)

                api(libs.slf4j.api)
                api(libs.kotlinx.serialization.json)
                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.datetime)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.common)
                implementation(projects.crashInterceptor)

                implementation(libs.kotest)
                implementation(libs.raysmith.utils)
            }
        }
    }
}