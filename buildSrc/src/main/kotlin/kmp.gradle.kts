plugins {
    kotlin("multiplatform")
    id("tests")
}

kotlin {
    jvmToolchain(17)
    jvm()

    sourceSets {
        configureEach {
            languageSettings {
//                optIn("kotlin.RequiresOptIn")
//                optIn("kotlin.ExperimentalStdlibApi")
//                optIn("kotlin.contracts.ExperimentalContracts")
//                optIn("kotlinx.serialization.ExperimentalSerializationApi")
//
                enableLanguageFeature("ContextReceivers")
                enableLanguageFeature("WhenGuards")
//                enableLanguageFeature("NestedTypeAliases")
            }
        }
    }
}