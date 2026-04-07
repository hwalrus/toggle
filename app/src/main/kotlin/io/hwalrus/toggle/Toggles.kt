package io.hwalrus.toggle

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.with
import org.http4k.lens.Header.LOCATION
import org.http4k.format.Jackson.autoBody
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.boolean
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

private data class ToggleState(val enabled: Boolean)

private val groupName = Path.of("group")
private val toggleName = Path.of("name")
private val toggleEnabled = Query.boolean().required("enabled")
private val allTogglesBody = autoBody<Map<String, Boolean>>().toLens()
private val toggleStateBody = autoBody<ToggleState>().toLens()

private fun StoreResult.toResponse() = when (this) {
    StoreResult.Success -> Response(OK)
    StoreResult.NotFound -> Response(NOT_FOUND)
}

fun toggleRoutes(store: ToggleStore): RoutingHttpHandler = routes(
    "" bind GET to { req ->
        val group = groupName(req)
        val toggles = store.getAll(group) ?: return@to Response(NOT_FOUND)
        Response(OK).with(allTogglesBody of toggles)
    },
    "/{name}" bind POST to { req ->
        val group = groupName(req)
        val name = toggleName(req)
        if (!namePattern.matches(name)) return@to Response(BAD_REQUEST)
        when (store.add(group, name, toggleEnabled(req))) {
            StoreResult.Success -> Response(CREATED).with(LOCATION of Uri.of("/group/$group/toggle/$name"))
            StoreResult.NotFound -> Response(NOT_FOUND)
        }
    },
    "/{name}" bind GET to { req ->
        when (val result = store.get(groupName(req), toggleName(req))) {
            GetResult.NotFound -> Response(NOT_FOUND)
            is GetResult.Found -> Response(OK).with(toggleStateBody of ToggleState(result.enabled))
        }
    },
    "/{name}/enable" bind POST to { req ->
        store.enable(groupName(req), toggleName(req)).toResponse()
    },
    "/{name}/disable" bind POST to { req ->
        store.disable(groupName(req), toggleName(req)).toResponse()
    },
    "/{name}" bind DELETE to { req ->
        store.delete(groupName(req), toggleName(req)).toResponse()
    }
)
