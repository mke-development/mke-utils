package team.mke.utils.ktor.ext.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import team.mke.utils.ktor.ext.root

class RouteRootTests : FreeSpec({
    "root on root route should return self and empty list" {
        testApplication {
            application {
                routing {
                    val (rootRoute, path) = root()
                    rootRoute shouldBe this
                    path.shouldBeEmpty()
                }
            }
        }
    }

    "root on nested route should return root route and list of descendants" {
        testApplication {
            application {
                routing {
                    var child1Route: Route? = null
                    var child2Route: Route? = null

                    route("api") {
                        child1Route = this
                        route("v1") {
                            child2Route = this
                            val (rootRoute, path) = root()
                            rootRoute shouldBe this@routing
                            path shouldBe listOf(child1Route, child2Route)
                        }
                    }
                }
            }
        }
    }
})
