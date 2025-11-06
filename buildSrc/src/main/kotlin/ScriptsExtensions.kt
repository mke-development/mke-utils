import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/** Applies jvm target with default configuration */
fun KotlinMultiplatformExtension.setupJvm(configuration: KotlinJvmTarget.() -> Unit = {}) {
    jvm {
        configuration()
    }
}

val PluginDependenciesSpecScope.`convention-kmp`: org.gradle.plugin.use.PluginDependencySpec get() = id("kmp")
val PluginDependenciesSpecScope.`convention-kmp-js`: org.gradle.plugin.use.PluginDependencySpec get() = id("kmp-js")
val PluginDependenciesSpecScope.`convention-tests`: org.gradle.plugin.use.PluginDependencySpec get() = id("tests")
