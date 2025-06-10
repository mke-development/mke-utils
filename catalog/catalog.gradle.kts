plugins {
    `version-catalog`
}

val projectName = name

catalog {
    versionCatalog {
        rootProject.subprojects.forEach {
            if (it.name != projectName) {
                library(it.name, "team.mke:mke-utils-${it.name}:${it.version}")
            }
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