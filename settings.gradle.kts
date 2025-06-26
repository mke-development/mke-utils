rootProject.name = "mke-utils"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
}


include("common")
include("bg")
include("crashInterceptor")
include("crashInterceptor-sentry")
include("db")
include("env")
include("io-validator")
include("server")
include("ktor-openapi")
include("ktor-extensions")
include("ktor-extensions-json")
include("ktor-extensions-xml")
include("ktor-client")
include("ktor-client-extensions-json")
include("ktor-server-options")
include("ktor-server-extensions-validator")
include("ktor-server-extensions-db")
include("logging")
include("mail")
include("serialization")
include("sms")
include("unmd")
include("json")
include("xml")
include("catalog")

rootProject.children.forEach {
    it.buildFileName = it.name + ".gradle.kts"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("kotlinWrappers") {
            val wrappersVersion = "2025.3.10"
            from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:$wrappersVersion")
        }
    }
}