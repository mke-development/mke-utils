plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(projects.json)

                api(libs.ktor.http)
                implementation(libs.ktor.server.core)
                implementation(libs.raysmith.utils)
            }
        }
    }
}