plugins {
    `convention-kmp`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.db)
                implementation(projects.io)

                implementation(libs.exposed.core)
                implementation(libs.exposed.dao)
                implementation(libs.kotest)
                implementation(libs.kotest.extensions.htmlreporter)
                implementation(libs.kotest.extensions.junitxml)
                implementation(libs.kotest.html.reporter)
                implementation(project.dependencies.platform(libs.testcontainers.bom))
                implementation(libs.testcontainers.core)
                implementation(libs.testcontainers.mariadb)
                implementation(libs.mariadb.connector)
                implementation(libs.reflections)
            }
        }
    }
}