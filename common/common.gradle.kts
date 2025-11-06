plugins {
    `convention-kmp-js`
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
//                implementation(libs.kotest.framework.engine)
//                implementation(libs.kotest.assertions.core)
            }
        }

        jvmMain {
            dependencies {
                api(projects.logging)
                api(projects.env)
                api(projects.crashInterceptor)

                api(libs.slf4j.api)
                implementation(libs.kotlin.reflect)
            }
        }

        jsMain {
            dependencies {
                implementation(kotlinWrappers.react)
            }
        }
    }
}