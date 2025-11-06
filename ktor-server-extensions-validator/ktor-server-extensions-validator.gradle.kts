plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(projects.ioValidator)

                api(libs.ktor.http)
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}