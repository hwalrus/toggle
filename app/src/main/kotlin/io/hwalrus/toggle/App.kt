package io.hwalrus.toggle

import org.http4k.core.HttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer

fun app(): HttpHandler = routes(
    "/toggle" bind toggleRoutes(InMemoryToggleStore())
)

fun main() {
    app().asServer(Netty(9000)).start()
}
