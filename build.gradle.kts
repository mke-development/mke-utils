import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.VersionCatalog
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    kotlin("multiplatform") apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.benManes.versions)
    alias(libs.plugins.publish)
    `version-catalog`
    `maven-publish`
}

group = "team.mke"
version = "3.1.0"

allprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")

    tasks {
        withType<Test> {
            useJUnitPlatform()
            jvmArgs(
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
            )
            filter {
                isFailOnNoMatchingTests = false
            }
            testLogging {
                showExceptions = true
                showStandardStreams = true
                events = setOf(
                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
                )
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }
    }
}

subprojects {
    val isCatalog = name == "catalog"

    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "org.jetbrains.dokka")
    if (isCatalog) {
        apply(plugin = "org.gradle.version-catalog")
    } else {
        apply(plugin = "org.jetbrains.kotlin.multiplatform")
    }

    group = rootProject.group
    version = rootProject.version

    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()
            coordinates(group.toString(), "${rootProject.name}-${name}", version.toString())

            if (isCatalog) {
                configure(VersionCatalog())
            } else {
                configure(KotlinMultiplatform(
                    javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
                    sourcesJar = true,
                ))
            }

            pom {
                name.set("MKE Utils • ${project.name}")
                description.set("MKE Utils • ${project.name}")
                url.set("https://github.com/mke-development/${rootProject.name}")


                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    connection.set("scm:https://github.com/mke-development/${rootProject.name}.git")
                    developerConnection.set("scm:git@github.com:mke-development/${rootProject.name}.git")
                    url.set("https://github.com/mke-development/${rootProject.name}")
                }

                developers {
                    developer {
                        id.set("RaySmith-ttc")
                        name.set("Ray Smith")
                        email.set("raysmith.ttcreate@gmail.com")
                    }
                }
            }
        }
    }
}

tasks {
    named<DependencyUpdatesTask>("dependencyUpdates").configure {
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val stableList = listOf("RELEASE", "FINAL", "GA")

        rejectVersionIf {
            val stableKeyword = stableList.any { candidate.version.uppercase().contains(it) }
            val isStable = stableKeyword || regex.matches(candidate.version)
            isStable.not()
        }
    }
}

dependencies {
    subprojects.forEach {
        kover(it)
    }
}

catalog {
    versionCatalog {
        subprojects.forEach {
            library(it.name, "team.mke:mke-utils-${it.name}:${it.version}")
        }

        bundle("ktor-client", listOf(
            projects.ktorClient,
            projects.ktorClientExtensionsJson,
        ).map { it.name })

        bundle("ktor-server", listOf(
            projects.ktorServerOptions,
            projects.ktorServerExtensionsDb,
            projects.ktorServerExtensionsValidator,
        ).map { it.name })
    }
}

publishing {
    publications {
        create<MavenPublication>("catalog") {
            artifactId = "${rootProject.name}-catalog"
            from(components["versionCatalog"])
        }
    }
}

mavenPublishing {
    configure(VersionCatalog())
}