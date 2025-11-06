plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(projects.env)
                api(libs.xmlutil.serialization)
            }
        }
    }
}