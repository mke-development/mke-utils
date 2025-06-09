plugins {
    kotlin("multiplatform")
}

kotlin {
    setupJvm()

    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    useFirefox()
                }
            }
        }
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonTest {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
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

        jvmTest {
            dependencies {
                implementation(libs.kotest)
            }
        }

        jsMain {
            dependencies {
                implementation(kotlinWrappers.react)
            }
        }
    }
}