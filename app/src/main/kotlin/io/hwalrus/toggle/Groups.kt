package io.hwalrus.toggle

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.with
import org.http4k.lens.Header.LOCATION
import org.http4k.format.Jackson.autoBody
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

private val groupName = Path.of("group")
private val newName = Query.required("name")
private val groupListBody = autoBody<List<String>>().toLens()

fun groupRoutes(store: ToggleStore): RoutingHttpHandler = routes(
    "" bind GET to {
        Response(OK).with(groupListBody of store.getGroups())
    },
    "/{group}" bind POST to { req ->
        val group = groupName(req)
        if (!namePattern.matches(group)) return@to Response(BAD_REQUEST)
        when (store.addGroup(group)) {
            GroupResult.Created -> Response(CREATED).with(LOCATION of Uri.of("/group/$group"))
            GroupResult.AlreadyExists -> Response(CONFLICT)
        }
    },
    "/{group}" bind DELETE to { req ->
        when (store.deleteGroup(groupName(req))) {
            StoreResult.Success -> Response(OK)
            StoreResult.NotFound -> Response(NOT_FOUND)
        }
    },
    "/{group}/rename" bind POST to { req ->
        val renamed = newName(req)
        if (!namePattern.matches(renamed)) return@to Response(BAD_REQUEST)
        when (store.renameGroup(groupName(req), renamed)) {
            StoreResult.Success -> Response(OK)
            StoreResult.NotFound -> Response(NOT_FOUND)
        }
    },
    "/{group}/toggle" bind toggleRoutes(store)
)
