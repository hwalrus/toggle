package io.hwalrus.toggle

import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.filter.CorsPolicy.Companion.UnsafeGlobalPermissive
import org.http4k.filter.ServerFilters
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Netty
import org.http4k.server.asServer

fun app(): HttpHandler = ServerFilters.Cors(UnsafeGlobalPermissive)
    .then(ServerFilters.CatchLensFailure)
    .then(
        routes(
            "/toggle" bind toggleRoutes(InMemoryToggleStore()),
            "/" bind static(Classpath("public"))
        )
    )

fun main() {
    app().asServer(Netty(10800)).start()
}
