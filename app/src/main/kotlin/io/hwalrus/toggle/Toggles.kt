package io.hwalrus.toggle

import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.boolean
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

private val toggleName = Path.of("name")
private val toggleEnabled = Query.boolean().required("enabled")

fun toggleRoutes(store: ToggleStore): RoutingHttpHandler = routes(
    "/{name}" bind POST to { req ->
        store.add(toggleName(req), toggleEnabled(req))
        Response(OK)
    },
    "/{name}" bind GET to { req ->
        Response(OK).body(store.isEnabled(toggleName(req)).toString())
    }
)
