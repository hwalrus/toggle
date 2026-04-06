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

    describe("GET /group") {
        it("returns empty list when no groups exist") {
            get("/group").use { response ->
                response.code shouldBe 200
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("[]")
            }
        }

        it("returns sorted list of groups") {
            post("/group/beta").use { it.code shouldBe 201 }
            post("/group/alpha").use { it.code shouldBe 201 }
            get("/group").use { response ->
                response.code shouldBe 200
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""["alpha","beta"]""")
            }
        }
    }

    describe("POST /group/{group}") {
        it("returns 201 Created with a Location header") {
            post("/group/payments").use { response ->
                response.code shouldBe 201
                response.header("Location") shouldBe "/group/payments"
            }
        }

        it("returns 409 when group already exists") {
            post("/group/payments").use { it.code shouldBe 201 }
            post("/group/payments").use { it.code shouldBe 409 }
        }
    }

    describe("POST /group/{group}/rename") {
        it("renames a group") {
            post("/group/old").use { it.code shouldBe 201 }
            post("/group/old/rename?name=new").use { it.code shouldBe 200 }
            get("/group").use { response ->
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""["new"]""")
            }
        }

        it("returns 404 for an unknown group") {
            post("/group/unknown/rename?name=new").use { it.code shouldBe 404 }
        }
    }

    describe("DELETE /group/{group}") {
        it("deletes an existing group") {
            post("/group/payments").use { it.code shouldBe 201 }
            delete("/group/payments").use { it.code shouldBe 200 }
            get("/group").use { response ->
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("[]")
            }
        }

        it("returns 404 for an unknown group") {
            delete("/group/unknown").use { it.code shouldBe 404 }
        }
    }

    describe("GET /group/{group}/toggle") {
        it("returns an empty JSON object when the group has no toggles") {
            post("/group/g").use { it.code shouldBe 201 }
            get("/group/g/toggle").use { response ->
                response.code shouldBe 200
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("{}")
            }
        }

        it("returns 404 for an unknown group") {
            get("/group/unknown/toggle").use { it.code shouldBe 404 }
        }
    }

    describe("POST /group/{group}/toggle/{name}") {
        it("returns 201 Created with a Location header") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature?enabled=true").use { response ->
                response.code shouldBe 201
                response.header("Location") shouldBe "/group/g/toggle/feature"
            }
        }

        it("adds an enabled toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature?enabled=true").use { it.code shouldBe 201 }
            get("/group/g/toggle/feature").use { response ->
                response.code shouldBe 200
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"enabled":true}""")
            }
        }

        it("adds a disabled toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature?enabled=false").use { it.code shouldBe 201 }
            get("/group/g/toggle/feature").use { response ->
                response.code shouldBe 200
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"enabled":false}""")
            }
        }

        it("returns 404 when group does not exist") {
            post("/group/nogroup/toggle/feature?enabled=true").use { it.code shouldBe 404 }
        }
    }

    describe("GET /group/{group}/toggle/{name}") {
        it("returns 404 for an unknown toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            get("/group/g/toggle/feature").use { it.code shouldBe 404 }
        }
    }

    describe("POST /group/{group}/toggle/{name}/enable") {
        it("enables a disabled toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature?enabled=false").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature/enable").use { it.code shouldBe 200 }
            get("/group/g/toggle/feature").use { response ->
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"enabled":true}""")
            }
        }

        it("returns 404 for an unknown toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature/enable").use { it.code shouldBe 404 }
        }
    }

    describe("POST /group/{group}/toggle/{name}/disable") {
        it("disables an enabled toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature?enabled=true").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature/disable").use { it.code shouldBe 200 }
            get("/group/g/toggle/feature").use { response ->
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"enabled":false}""")
            }
        }

        it("returns 404 for an unknown toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature/disable").use { it.code shouldBe 404 }
        }
    }

    describe("DELETE /group/{group}/toggle/{name}") {
        it("deletes an existing toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/feature?enabled=true").use { it.code shouldBe 201 }
            delete("/group/g/toggle/feature").use { it.code shouldBe 200 }
            delete("/group/g/toggle/feature").use { it.code shouldBe 404 }
        }

        it("returns 404 for an unknown toggle") {
            post("/group/g").use { it.code shouldBe 201 }
            delete("/group/g/toggle/feature").use { it.code shouldBe 404 }
        }
    }

    describe("GET /group/{group}/toggle (all)") {
        it("returns all toggles in the group") {
            post("/group/g").use { it.code shouldBe 201 }
            post("/group/g/toggle/a?enabled=true").use { it.code shouldBe 201 }
            post("/group/g/toggle/b?enabled=false").use { it.code shouldBe 201 }
            get("/group/g/toggle").use { response ->
                Jackson.parse(response.bodyText()) shouldBe Jackson.parse("""{"a":true,"b":false}""")
            }
        }
    }
})
