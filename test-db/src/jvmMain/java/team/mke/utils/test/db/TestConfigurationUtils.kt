package team.mke.utils.test.db

import io.kotest.common.KotestInternal
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.config.DefaultTestConfig
import io.kotest.core.test.parents
import kotlin.collections.any
import kotlin.text.endsWith
import kotlin.text.startsWith

@OptIn(KotestInternal::class)
@DevelopmentOnly
fun Spec.filterTests(predicate: (TestCase) -> Boolean) {
    defaultTestConfig = DefaultTestConfig(enabledIf = predicate)
}

@OptIn(KotestInternal::class)
@DevelopmentOnly
fun Spec.filterTestsStartsWith(startsWith: String) = filterTests { it.name.name.startsWith(startsWith) }

@OptIn(KotestInternal::class)
@DevelopmentOnly
fun Spec.filterTestsGroups(startWith: String) = filterTests {
    it.name.name.startsWith(startWith) || it.parents().any { it.name.name.startsWith(startWith) }
}

@OptIn(KotestInternal::class)
@DevelopmentOnly
fun Spec.filterTestsEndWith(endsWith: String) = filterTests { it.name.name.endsWith(endsWith) }

@OptIn(KotestInternal::class)
@DevelopmentOnly
fun Spec.filterTestsStartsAndEndWith(startsWith: String, endsWith: String) = filterTests {
    it.name.name.startsWith(startsWith) && it.name.name.endsWith(endsWith)
}