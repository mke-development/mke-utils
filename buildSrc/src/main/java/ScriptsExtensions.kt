import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/** Applies jvm target with default configuration */
fun KotlinMultiplatformExtension.setupJvm(configuration: KotlinJvmTarget.() -> Unit = {}) {
    jvm {
        withJava()
        configuration()
    }
}