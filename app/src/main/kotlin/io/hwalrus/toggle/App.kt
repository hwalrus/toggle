package io.hwalrus.toggle

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer

val app: HttpHandler = routes(
    "/hello" bind GET to { _: Request -> Response(OK).body("Hello, World!") }
)

fun main() {
    app.asServer(Netty(9000)).start()
}
