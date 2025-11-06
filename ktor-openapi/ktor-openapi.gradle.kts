plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(libs.ktor.openapi.tools.openapi)
                api(libs.kenerator.reflection)
                api(libs.kenerator.swagger)
                api(libs.kenerator.serialization)

                implementation(projects.serialization)

                implementation(libs.ktor.http)
                implementation(libs.kotlin.reflect)
                implementation(libs.kenerator.core)
                implementation(libs.swagger.models)
            }
        }
    }
}