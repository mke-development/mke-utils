plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.raysmith.utils)
                implementation(libs.slf4j.api)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }
    }
}