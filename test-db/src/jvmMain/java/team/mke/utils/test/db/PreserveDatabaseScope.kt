package team.mke.utils.test.db

import io.kotest.common.KotestInternal
import io.kotest.core.Tag
import io.kotest.core.names.TestNameBuilder
import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import io.kotest.core.spec.style.scopes.FreeSpecRootScope
import io.kotest.core.spec.style.scopes.addContainer
import io.kotest.core.test.config.TestConfig

/**
 * Регистрирует тестовый контейнер внутри другого контейнера и древо дочерних тестов, в котором не удаляются данные базы данных.
 * */
context(FreeSpecContainerScope)
@OptIn(KotestInternal::class)
suspend infix operator fun String.times(test: suspend FreeSpecContainerScope.() -> Unit) {
    registerContainer(
        name = TestNameBuilder.builder(this).build(),
        disabled = false,
        config = TestConfig(tags = setOf(preserveDatabaseTag))
    ) { FreeSpecContainerScope(this).test() }
}

/**
 * Регистрирует тестовый контейнер в FreeSpec и древо дочерних тестов, в котором не удаляются данные базы данных
 * */
context(FreeSpecRootScope)
@OptIn(KotestInternal::class)
infix operator fun String.timesAssign(test: suspend FreeSpecContainerScope.() -> Unit) {
    addContainer(
        testName = TestNameBuilder.builder(this).build(),
        disabled = false,
        config = TestConfig(tags = setOf(preserveDatabaseTag))
    ) { FreeSpecContainerScope(this).test() }
}

val preserveDatabaseTag = Tag("preserve-database")