package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.http4k.core.ContentType
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson

class AppTest : DescribeSpec({
    describe("GET /group") {
        it("returns an empty JSON array when no groups exist") {
            val response = app()(Request(GET, "/group"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("[]")
        }

        it("returns all group names sorted") {
            val handler = app()
            handler(Request(POST, "/group/beta"))
            handler(Request(POST, "/group/alpha"))
            val response = handler(Request(GET, "/group"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""["alpha","beta"]""")
        }
    }

    describe("POST /group/{group}") {
        it("returns 201 Created with a Location header") {
            val response = app()(Request(POST, "/group/payments"))
            response.status shouldBe CREATED
            response.header("Location") shouldBe "/group/payments"
        }

        it("returns 409 Conflict when group already exists") {
            val handler = app()
            handler(Request(POST, "/group/payments"))
            handler(Request(POST, "/group/payments")).status shouldBe CONFLICT
        }

        it("returns 400 when group name contains invalid characters") {
            app()(Request(POST, "/group/bad.name")).status shouldBe BAD_REQUEST
        }

        it("returns 400 when group name exceeds 100 characters") {
            app()(Request(POST, "/group/${"a".repeat(101)}")).status shouldBe BAD_REQUEST
        }
    }

    describe("POST /group/{group}/rename") {
        it("renames an existing group") {
            val handler = app()
            handler(Request(POST, "/group/old"))
            handler(Request(POST, "/group/old?enabled=true"))
            val response = handler(Request(POST, "/group/old/rename?name=new"))
            response.status shouldBe OK
            Jackson.parse(handler(Request(GET, "/group")).bodyString()) shouldBe Jackson.parse("""["new"]""")
        }

        it("returns 404 for an unknown group") {
            app()(Request(POST, "/group/unknown/rename?name=new")).status shouldBe NOT_FOUND
        }

        it("returns 400 when new name is invalid") {
            val handler = app()
            handler(Request(POST, "/group/mygroup"))
            handler(Request(POST, "/group/mygroup/rename?name=bad name")).status shouldBe BAD_REQUEST
        }
    }

    describe("DELETE /group/{group}") {
        it("deletes an existing group") {
            val handler = app()
            handler(Request(POST, "/group/payments"))
            handler(Request(DELETE, "/group/payments")).status shouldBe OK
            Jackson.parse(handler(Request(GET, "/group")).bodyString()) shouldBe Jackson.parse("[]")
        }

        it("returns 404 for an unknown group") {
            app()(Request(DELETE, "/group/unknown")).status shouldBe NOT_FOUND
        }
    }

    describe("GET /group/{group}/toggle") {
        it("returns an empty JSON object when the group has no toggles") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            val response = handler(Request(GET, "/group/g/toggle"))
            response.status shouldBe OK
            response.header("Content-Type") shouldBe "${ContentType.APPLICATION_JSON.value}; charset=utf-8"
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("{}")
        }

        it("returns all toggles in the group") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/a?enabled=true"))
            handler(Request(POST, "/group/g/toggle/b?enabled=false"))
            val response = handler(Request(GET, "/group/g/toggle"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"a":true,"b":false}""")
        }

        it("does not include deleted toggles") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/a?enabled=true"))
            handler(Request(POST, "/group/g/toggle/b?enabled=true"))
            handler(Request(DELETE, "/group/g/toggle/a"))
            Jackson.parse(handler(Request(GET, "/group/g/toggle")).bodyString()) shouldBe Jackson.parse("""{"b":true}""")
        }

        it("returns 404 for an unknown group") {
            app()(Request(GET, "/group/unknown/toggle")).status shouldBe NOT_FOUND
        }
    }

    describe("/group/{group}/toggle/{name}") {
        it("returns 404 for an unknown toggle") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(GET, "/group/g/toggle/myFeature")).status shouldBe NOT_FOUND
        }

        it("returns 201 Created with a Location header") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            val response = handler(Request(POST, "/group/g/toggle/myFeature?enabled=true"))
            response.status shouldBe CREATED
            response.header("Location") shouldBe "/group/g/toggle/myFeature"
        }

        it("returns 404 when the group does not exist") {
            app()(Request(POST, "/group/nogroup/toggle/myFeature?enabled=true")).status shouldBe NOT_FOUND
        }

        it("returns 400 when enabled param is missing") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature")).status shouldBe BAD_REQUEST
        }

        it("returns 400 when enabled param is not a boolean") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature?enabled=notabool")).status shouldBe BAD_REQUEST
        }

        it("returns 400 when toggle name contains spaces") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/bad%20name?enabled=true")).status shouldBe BAD_REQUEST
        }

        it("returns 400 when toggle name exceeds 100 characters") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/${"a".repeat(101)}?enabled=true")).status shouldBe BAD_REQUEST
        }

        it("toggle names are unique per group, not globally") {
            val handler = app()
            handler(Request(POST, "/group/g1"))
            handler(Request(POST, "/group/g2"))
            handler(Request(POST, "/group/g1/toggle/feat?enabled=true")).status shouldBe CREATED
            handler(Request(POST, "/group/g2/toggle/feat?enabled=false")).status shouldBe CREATED
            Jackson.parse(handler(Request(GET, "/group/g1/toggle/feat")).bodyString()) shouldBe Jackson.parse("""{"enabled":true}""")
            Jackson.parse(handler(Request(GET, "/group/g2/toggle/feat")).bodyString()) shouldBe Jackson.parse("""{"enabled":false}""")
        }

        it("adds a toggle and returns its enabled state") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature?enabled=true"))
            val response = handler(Request(GET, "/group/g/toggle/myFeature"))
            response.status shouldBe OK
            Jackson.parse(response.bodyString()) shouldBe Jackson.parse("""{"enabled":true}""")
        }

        it("returns 409 Conflict when toggle name already exists in the group") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature?enabled=true"))
            handler(Request(POST, "/group/g/toggle/myFeature?enabled=false")).status shouldBe CONFLICT
        }
    }

    describe("/group/{group}/toggle/{name}/enable") {
        it("returns 404 when the toggle does not exist") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature/enable")).status shouldBe NOT_FOUND
        }

        it("enables a disabled toggle") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature?enabled=false"))
            handler(Request(POST, "/group/g/toggle/myFeature/enable"))
            Jackson.parse(handler(Request(GET, "/group/g/toggle/myFeature")).bodyString()) shouldBe Jackson.parse("""{"enabled":true}""")
        }
    }

    describe("/group/{group}/toggle/{name}/disable") {
        it("returns 404 when the toggle does not exist") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature/disable")).status shouldBe NOT_FOUND
        }

        it("disables an enabled toggle") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature?enabled=true"))
            handler(Request(POST, "/group/g/toggle/myFeature/disable"))
            Jackson.parse(handler(Request(GET, "/group/g/toggle/myFeature")).bodyString()) shouldBe Jackson.parse("""{"enabled":false}""")
        }
    }

    describe("DELETE /group/{group}/toggle/{name}") {
        it("returns 404 when the toggle does not exist") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(DELETE, "/group/g/toggle/myFeature")).status shouldBe NOT_FOUND
        }

        it("deletes an existing toggle") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            handler(Request(POST, "/group/g/toggle/myFeature?enabled=true"))
            handler(Request(DELETE, "/group/g/toggle/myFeature")).status shouldBe OK
            handler(Request(DELETE, "/group/g/toggle/myFeature")).status shouldBe NOT_FOUND
        }
    }

    describe("security headers") {
        it("API responses include X-Content-Type-Options, X-Frame-Options, Cache-Control") {
            val handler = app()
            handler(Request(POST, "/group/g"))
            val response = handler(Request(GET, "/group/g/toggle"))
            response.header("X-Content-Type-Options") shouldBe "nosniff"
            response.header("X-Frame-Options") shouldBe "DENY"
            response.header("Cache-Control") shouldBe "no-store"
        }
    }

    describe("CORS") {
        it("reflects the configured origin when ALLOWED_ORIGIN is set") {
            val response = app(allowedOrigin = "https://example.com")(
                Request(GET, "/group").header("Origin", "https://example.com")
            )
            response.header("Access-Control-Allow-Origin") shouldBe "https://example.com"
        }

        it("does not reflect an unconfigured origin") {
            val response = app(allowedOrigin = "https://example.com")(
                Request(GET, "/group").header("Origin", "https://evil.com")
            )
            response.header("Access-Control-Allow-Origin") shouldNotBe "https://evil.com"
        }
    }
})
