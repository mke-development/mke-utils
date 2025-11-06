plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(libs.slf4j.api)
                api(libs.javax.mail)
            }
        }

        jvmTest {
            dependencies {
                implementation(projects.env)

                implementation(libs.kotest)
                implementation(libs.raysmith.utils)
            }
        }
    }
}