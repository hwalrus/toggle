package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.http4k.format.Jackson
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer

class ToggleEndToEndTest : DescribeSpec({
    val client = OkHttpClient()
    val store = InMemoryToggleStore()
    lateinit var baseUrl: String
    lateinit var server: Http4kServer

    beforeSpec {
        server = app(store).asServer(Netty(0)).start()
        baseUrl = "http://localhost:${server.port()}"
    }

    afterSpec {
        server.stop()
        client.dispatcher.executorService.shutdown()
    }

    beforeEach {
        store.clear()
    }

    fun get(path: String): Response =
        client.newCall(Request.Builder().url("$baseUrl$path").get().build()).execute()

    fun post(path: String): Response =
        client.newCall(Request.Builder().url("$baseUrl$path").post("".toRequestBody()).build()).execute()

    fun delete(path: String): Response =
        client.newCall(Request.Builder().url("$baseUrl$path").delete().build()).execute()

    fun Response.bodyText(): String = checkNotNull(body).string()

    describe("GET /toggle") {
        it("returns an empty JSON object when no toggles exist") {
            get("/toggle").use { response ->
                response.code shouldBe 200
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("{}")
            }
        }
    }

    describe("POST /toggle/{name}") {
        it("returns 201 Created with a Location header") {
            post("/toggle/feature?enabled=true").use { response ->
                response.code shouldBe 201
                response.header("Location") shouldBe "/toggle/feature"
            }
        }

        it("adds an enabled toggle") {
            post("/toggle/feature?enabled=true").use { it.code shouldBe 201 }
            get("/toggle/feature").use { response ->
                response.code shouldBe 200
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"enabled":true}""")
            }
        }

        it("adds a disabled toggle") {
            post("/toggle/feature?enabled=false").use { it.code shouldBe 201 }
            get("/toggle/feature").use { response ->
                response.code shouldBe 200
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"enabled":false}""")
            }
        }
    }

    describe("GET /toggle/{name}") {
        it("returns 404 for an unknown toggle") {
            get("/toggle/feature").use { response ->
                response.code shouldBe 404
            }
        }
    }

    describe("POST /toggle/{name}/enable") {
        it("enables a disabled toggle") {
            post("/toggle/feature?enabled=false").use { it.code shouldBe 201 }
            post("/toggle/feature/enable").use { it.code shouldBe 200 }
            get("/toggle/feature").use { response ->
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"enabled":true}""")
            }
        }

        it("returns 404 for an unknown toggle") {
            post("/toggle/feature/enable").use { it.code shouldBe 404 }
        }
    }

    describe("POST /toggle/{name}/disable") {
        it("disables an enabled toggle") {
            post("/toggle/feature?enabled=true").use { it.code shouldBe 201 }
            post("/toggle/feature/disable").use { it.code shouldBe 200 }
            get("/toggle/feature").use { response ->
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"enabled":false}""")
            }
        }

        it("returns 404 for an unknown toggle") {
            post("/toggle/feature/disable").use { it.code shouldBe 404 }
        }
    }

    describe("DELETE /toggle/{name}") {
        it("deletes an existing toggle") {
            post("/toggle/feature?enabled=true").use { it.code shouldBe 201 }
            delete("/toggle/feature").use { it.code shouldBe 200 }
            delete("/toggle/feature").use { it.code shouldBe 404 }
        }

        it("returns 404 for an unknown toggle") {
            delete("/toggle/feature").use { it.code shouldBe 404 }
        }
    }

    describe("GET /toggle (all)") {
        it("returns all toggles") {
            post("/toggle/a?enabled=true").use { it.code shouldBe 201 }
            post("/toggle/b?enabled=false").use { it.code shouldBe 201 }
            get("/toggle").use { response ->
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"a":true,"b":false}""")
            }
        }
    }
})
