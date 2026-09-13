plugins {
    `convention-kmp`
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(projects.db)
                api(projects.json)

                api(libs.kotlinx.serialization.json)
                api(libs.raysmith.exposedOption)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }
    }
}
