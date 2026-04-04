package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.http4k.format.Jackson
import org.http4k.server.Netty
import org.http4k.server.asServer

class ToggleEndToEndTest : DescribeSpec({
    val server = autoClose(app().asServer(Netty(0)).start())
    val client = OkHttpClient()
    val baseUrl = "http://localhost:${server.port()}"

    afterSpec { client.dispatcher.executorService.shutdown() }

    fun get(path: String): Response =
        client.newCall(Request.Builder().url("$baseUrl$path").get().build()).execute()

    fun post(path: String): Response =
        client.newCall(Request.Builder().url("$baseUrl$path").post("".toRequestBody()).build()).execute()

    fun delete(path: String): Response =
        client.newCall(Request.Builder().url("$baseUrl$path").delete().build()).execute()

    describe("GET /toggle") {
        it("returns an empty JSON object when no toggles exist") {
            get("/toggle").use { response ->
                response.code shouldBe 200
                Jackson.parse(checkNotNull(response.body).string()) shouldBe Jackson.parse("{}")
            }
        }
    }

    describe("POST /toggle/{name}") {
        it("adds an enabled toggle") {
            post("/toggle/e2e-add-enabled?enabled=true").use { it.code shouldBe 200 }
            get("/toggle/e2e-add-enabled").use { response ->
                response.code shouldBe 200
                checkNotNull(response.body).string() shouldBe "true"
            }
        }

        it("adds a disabled toggle") {
            post("/toggle/e2e-add-disabled?enabled=false").use { it.code shouldBe 200 }
            get("/toggle/e2e-add-disabled").use { response ->
                response.code shouldBe 200
                checkNotNull(response.body).string() shouldBe "false"
            }
        }
    }

    describe("GET /toggle/{name}") {
        it("returns false for an unknown toggle") {
            get("/toggle/e2e-unknown").use { response ->
                response.code shouldBe 200
                checkNotNull(response.body).string() shouldBe "false"
            }
        }
    }

    describe("POST /toggle/{name}/enable") {
        it("enables a disabled toggle") {
            post("/toggle/e2e-enable?enabled=false").use { it.code shouldBe 200 }
            post("/toggle/e2e-enable/enable").use { it.code shouldBe 200 }
            get("/toggle/e2e-enable").use { response ->
                checkNotNull(response.body).string() shouldBe "true"
            }
        }

        it("returns 404 for an unknown toggle") {
            post("/toggle/e2e-enable-unknown/enable").use { it.code shouldBe 404 }
        }
    }

    describe("POST /toggle/{name}/disable") {
        it("disables an enabled toggle") {
            post("/toggle/e2e-disable?enabled=true").use { it.code shouldBe 200 }
            post("/toggle/e2e-disable/disable").use { it.code shouldBe 200 }
            get("/toggle/e2e-disable").use { response ->
                checkNotNull(response.body).string() shouldBe "false"
            }
        }

        it("returns 404 for an unknown toggle") {
            post("/toggle/e2e-disable-unknown/disable").use { it.code shouldBe 404 }
        }
    }

    describe("DELETE /toggle/{name}") {
        it("deletes an existing toggle") {
            post("/toggle/e2e-delete?enabled=true").use { it.code shouldBe 200 }
            delete("/toggle/e2e-delete").use { it.code shouldBe 200 }
            delete("/toggle/e2e-delete").use { it.code shouldBe 404 }
        }

        it("returns 404 for an unknown toggle") {
            delete("/toggle/e2e-delete-unknown").use { it.code shouldBe 404 }
        }
    }

    describe("GET /toggle (all)") {
        it("returns all toggles") {
            post("/toggle/e2e-all-a?enabled=true").use { it.code shouldBe 200 }
            post("/toggle/e2e-all-b?enabled=false").use { it.code shouldBe 200 }
            get("/toggle").use { response ->
                val body = Jackson.parse(checkNotNull(response.body).string())
                body["e2e-all-a"].booleanValue() shouldBe true
                body["e2e-all-b"].booleanValue() shouldBe false
            }
        }
    }
})
