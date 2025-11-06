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
                api(libs.raysmith.utils)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }
    }
}