plugins {
    alias(libs.plugins.convention.kmp)
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.icu4j)
            }
        }
    }
}
