plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(projects.env)
                api(projects.logging)
                api(projects.crashInterceptor)
                api(projects.common)

                implementation(libs.slf4j.api)
                implementation(libs.raysmith.utils)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest)
                implementation(libs.mockk)
            }
        }
    }
}