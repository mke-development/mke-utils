plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(libs.tika.core)
                implementation(libs.apache.commons.imaging)
            }
        }
    }
}