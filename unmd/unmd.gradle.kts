plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.markdown)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }
    }
}