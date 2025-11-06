plugins {
    `convention-kmp`
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.model)

                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}