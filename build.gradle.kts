import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    java
    signing
    `maven-publish`
    alias(libs.plugins.nmcp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.benManes.versions)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

group = "team.mke"
version = "2.15.1"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kover {
    useJacoco()
    reports {
        total {
            xml {
                onCheck = true
            }
            html {
                onCheck = true
            }
        }
    }

}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }

    jvm {
        withJava()
        withSourcesJar()
    }

    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    useFirefox()
                }
            }
        }
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {

            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
            }
        }

        jvmMain {
            dependencies {
                api(libs.slf4j.api)
                api(libs.sentry)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
                api(libs.ktor.client.core)
                api(libs.ktor.client.logging)
                api(libs.ktor.client.auth)
                api(libs.ktor.client.okhttp)
                api(libs.ktor.client.contentNegotiation)
                api(libs.ktor.serialization.kotlinx.xml)
                api(libs.ktor.serialization.kotlinx.json)
                api(libs.xmlutil.serialization.jvm)
                api(libs.exposed.core)
                api(libs.exposed.dao)
                api(libs.exposed.java.time)
                api(libs.hikari)
                api(libs.ktor.server.core.jvm)
                api(libs.javax.mail)
                api(libs.markdown)
                api(libs.reflections)
                api(libs.tika.core)
                api(libs.apache.commons.imaging)

                api(libs.raysmith.utils)
                api(libs.raysmith.exposedOption)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotest)
                implementation(libs.raysmith.utils)
                implementation(libs.kotest.assertions.core)
                implementation(libs.ktor.client.okhttp)
                implementation(kotlin("reflect"))

                implementation(libs.slf4j.api)
                implementation(libs.log4j.slf4j2.impl)
                implementation(libs.mockk)
                implementation(libs.log4j.core)
            }
        }

        jsMain {
            dependencies {
                implementation(kotlinWrappers.react)
            }
        }
    }
}

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

    withType<PublishToMavenRepository> {
        dependsOn(check)
    }
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

publishing {
    publications {
        withType<MavenPublication> {
            groupId = project.group.toString()
            version = project.version.toString()
            artifact(project.tasks.register("${name}DokkaJar", Jar::class) {
                group = JavaBasePlugin.DOCUMENTATION_GROUP
                description = "Assembles Kotlin docs with Dokka into a Javadoc jar"
                archiveClassifier.set("javadoc")
                from(tasks.named("dokkaHtml"))
                archiveBaseName.set("${archiveBaseName.get()}-${name}")
            })

            pom {
                packaging = "jar"
                name.set("MKE Utils")
                url.set("https://github.com/MKE-overseas/mke-utils")
                description.set("MKE module with utils")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    connection.set("scm:https://github.com/MKE-overseas/mke-utils.git")
                    developerConnection.set("scm:git@github.com:MKE-overseas/mke-utils.git")
                    url.set("https://github.com/MKE-overseas/mke-utils")
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
    repositories {
        maven {
            name = "OSSRH"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().matches(".*(SNAPSHOT|rc.\\d+)".toRegex())) snapshotsUrl else releasesUrl
            credentials {
                username = System.getenv("SONATYPE_USER")
                password = System.getenv("SONATYPE_PASS")
            }
        }
    }
}

signing {
    publishing.publications.forEach {
        sign(it)
    }
}

nmcp {
    publishAllPublications {
        username.set(System.getenv("CENTRAL_SONATYPE_USER"))
        password.set(System.getenv("CENTRAL_SONATYPE_PASS"))
        publicationType.set("AUTOMATIC")
    }
}