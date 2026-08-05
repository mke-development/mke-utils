import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

tasks {
    withType<Test>() {
        useJUnitPlatform()
        failOnNoDiscoveredTests.set(false)
        jvmArgs(
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
        )
        testLogging {
            showExceptions = true
            showStandardStreams = true
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED
            )
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    withType<KotlinJsTest>() {
        failOnNoDiscoveredTests.set(false)
    }
}
