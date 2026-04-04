package io.hwalrus.toggle

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.autoBody
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.boolean
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

private val toggleName = Path.of("name")
private val toggleEnabled = Query.boolean().required("enabled")
private val allTogglesBody = autoBody<Map<String, Boolean>>().toLens()

private fun UpdateResult.toResponse() = when (this) {
    UpdateResult.Updated -> Response(OK)
    UpdateResult.NotFound -> Response(NOT_FOUND)
}

fun toggleRoutes(store: ToggleStore): RoutingHttpHandler = routes(
    "" bind GET to {
        Response(OK).with(allTogglesBody of store.getAll())
    },
    "/{name}" bind POST to { req ->
        store.add(toggleName(req), toggleEnabled(req))
        Response(OK)
    },
    "/{name}" bind GET to { req ->
        Response(OK).body(store.isEnabled(toggleName(req)).toString())
    },
    "/{name}/enable" bind POST to { req ->
        store.enable(toggleName(req)).toResponse()
    },
    "/{name}/disable" bind POST to { req ->
        store.disable(toggleName(req)).toResponse()
    },
    "/{name}" bind DELETE to { req ->
        store.delete(toggleName(req)).toResponse()
    }
)
