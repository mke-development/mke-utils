pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files(rootDir.parentFile.resolve("gradle/libs.versions.toml")))
        }
    }
}