plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.model)
            }
        }
        jvmMain {
            dependencies {
                api(projects.ktorOpenapi)
                api(projects.crashInterceptor)
                api(projects.json)
                api(projects.common)

                api(libs.ktor.server.core)
                api(libs.raysmith.exposedOption)
                implementation(libs.raysmith.utils)
            }
        }
    }
}