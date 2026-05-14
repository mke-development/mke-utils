plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.env)
                implementation(projects.model)
                implementation(libs.ktor.server.core)
                implementation(libs.raysmith.utils)
            }
        }
    }
}
