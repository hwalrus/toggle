package io.hwalrus.toggle

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.CorsPolicy.Companion.UnsafeGlobalPermissive
import org.http4k.filter.Only
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Netty
import org.http4k.server.asServer

private val securityHeaders = Filter { next ->
    { req ->
        next(req)
            .header("X-Content-Type-Options", "nosniff")
            .header("X-Frame-Options", "DENY")
            .header("Cache-Control", "no-store")
    }
}

fun app(allowedOrigin: String? = System.getenv("ALLOWED_ORIGIN")): HttpHandler {
    val corsPolicy = if (!allowedOrigin.isNullOrBlank()) {
        CorsPolicy(OriginPolicy.Only(allowedOrigin), listOf("content-type"), listOf(GET, POST, DELETE))
    } else {
        UnsafeGlobalPermissive
    }

    return ServerFilters.Cors(corsPolicy)
        .then(ServerFilters.CatchLensFailure)
        .then(
            routes(
                "/toggle" bind securityHeaders.then(toggleRoutes(InMemoryToggleStore())),
                "/" bind static(Classpath("public"))
            )
        )
}

fun main() {
    app().asServer(Netty(10800)).start()
}
