import org.gradle.api.tasks.testing.Test

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
        jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
        testLogging {
            showStandardStreams = true
        }
    }
}
