plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.model)
            }
        }
        jvmMain {
            dependencies {
                api(libs.ktor.server.core)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }
    }
}
