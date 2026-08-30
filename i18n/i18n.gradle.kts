plugins {
    alias(libs.plugins.convention.kmp)
    alias(libs.plugins.convention.tests)
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(libs.icu4j)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }
    }
}
