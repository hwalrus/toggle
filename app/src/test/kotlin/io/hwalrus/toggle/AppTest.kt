package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.ContentType
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson

class AppTest : DescribeSpec({
    describe("GET /toggle") {
        it("returns an empty JSON object when no toggles exist") {
            val response = app()(Request(GET, "/toggle"))
            response.status shouldBe OK
            response.header("Content-Type") shouldBe "${ContentType.APPLICATION_JSON.value}; charset=utf-8"
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("{}")
        }

        it("returns all toggles as a JSON object") {
            val handler = app()
            handler(Request(POST, "/toggle/a?enabled=true"))
            handler(Request(POST, "/toggle/b?enabled=false"))
            val response = handler(Request(GET, "/toggle"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"a":true,"b":false}""")
        }

        it("does not include deleted toggles") {
            val handler = app()
            handler(Request(POST, "/toggle/a?enabled=true"))
            handler(Request(POST, "/toggle/b?enabled=true"))
            handler(Request(DELETE, "/toggle/a"))
            val response = handler(Request(GET, "/toggle"))
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"b":true}""")
        }
    }

    describe("/toggle") {
        val handler = app()

        it("returns false for an unknown toggle") {
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"enabled":false}""")
        }

        it("adds a toggle and returns its enabled state") {
            handler(Request(POST, "/toggle/myFeature?enabled=true"))
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"enabled":true}""")
        }

        it("adds a disabled toggle and returns false") {
            handler(Request(POST, "/toggle/myFeature?enabled=false"))
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"enabled":false}""")
        }

        it("overwriting a toggle reflects the latest value") {
            handler(Request(POST, "/toggle/myFeature?enabled=true"))
            handler(Request(POST, "/toggle/myFeature?enabled=false"))
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"enabled":false}""")
        }
    }

    describe("/toggle/{name}/enable") {
        it("returns 404 when the toggle does not exist") {
            val response = app()(Request(POST, "/toggle/myFeature/enable"))
            response.status shouldBe NOT_FOUND
        }

        it("enables a disabled toggle") {
            val handler = app()
            handler(Request(POST, "/toggle/myFeature?enabled=false"))
            handler(Request(POST, "/toggle/myFeature/enable"))
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"enabled":true}""")
        }
    }

    describe("DELETE /toggle/{name}") {
        it("returns 404 when the toggle does not exist") {
            val response = app()(Request(DELETE, "/toggle/myFeature"))
            response.status shouldBe NOT_FOUND
        }

        it("deletes an existing toggle") {
            val handler = app()
            handler(Request(POST, "/toggle/myFeature?enabled=true"))
            handler(Request(DELETE, "/toggle/myFeature")).status shouldBe OK
            handler(Request(DELETE, "/toggle/myFeature")).status shouldBe NOT_FOUND
        }
    }

    describe("/toggle/{name}/disable") {
        it("returns 404 when the toggle does not exist") {
            val response = app()(Request(POST, "/toggle/myFeature/disable"))
            response.status shouldBe NOT_FOUND
        }

        it("disables an enabled toggle") {
            val handler = app()
            handler(Request(POST, "/toggle/myFeature?enabled=true"))
            handler(Request(POST, "/toggle/myFeature/disable"))
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"enabled":false}""")
        }
    }
})
