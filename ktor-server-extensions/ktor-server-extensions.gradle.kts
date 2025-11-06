plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(libs.ktor.server.core)
            }
        }
    }
}