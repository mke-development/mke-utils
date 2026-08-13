package team.mke.utils.test.db

import io.kotest.common.KotestInternal
import io.kotest.core.Tag
import io.kotest.core.names.TestNameBuilder
import io.kotest.core.spec.TestDefinitionBuilder
import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import io.kotest.core.spec.style.scopes.FreeSpecRootScope
import io.kotest.core.test.TestType
import io.kotest.core.test.config.TestConfig

val preserveDatabaseTag = Tag("preserve-database")

/**
 * Регистрирует тестовый контейнер внутри другого контейнера и древо дочерних тестов, в котором не удаляются данные базы данных.
 * */
@OptIn(KotestInternal::class)
context(scope: FreeSpecContainerScope)
suspend infix operator fun String.times(test: suspend FreeSpecContainerScope.() -> Unit) {
    val testName = TestNameBuilder.builder(this).build()
    val config = TestConfig(tags = setOf(preserveDatabaseTag))

    @Suppress("UNCHECKED_CAST")
    scope.registerTest(
        TestDefinitionBuilder.builder(testName, TestType.Container)
            .withConfig(config)
            .build { FreeSpecContainerScope(this).test() }
    )
}

/**
 * Регистрирует тестовый контейнер в FreeSpec и древо дочерних тестов, в котором не удаляются данные базы данных
 * */
@OptIn(KotestInternal::class)
context(scope: FreeSpecRootScope)
infix operator fun String.timesAssign(test: suspend FreeSpecContainerScope.() -> Unit) {
    val testName = TestNameBuilder.builder(this).build()
    val config = TestConfig(tags = setOf(preserveDatabaseTag))

    @Suppress("UNCHECKED_CAST")
    scope.add(
        TestDefinitionBuilder.builder(testName, TestType.Container)
            .withConfig(config)
            .build { FreeSpecContainerScope(this).test() }
    )
}
