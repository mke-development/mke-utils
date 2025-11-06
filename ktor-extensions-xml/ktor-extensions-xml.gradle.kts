plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(projects.xml)

                api(libs.ktor.http)
                implementation(libs.ktor.server.core)
            }
        }
    }
}