import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform")
    id("tests")
}

kotlin {
    jvmToolchain(17)
    jvm()
}
