plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(libs.slf4j.api)
            }
        }
    }
}