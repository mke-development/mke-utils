plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.env)

                api(libs.ktor.server.core)
            }
        }
    }
}