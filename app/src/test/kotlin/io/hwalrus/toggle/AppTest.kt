package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK

class AppTest : DescribeSpec({
    describe("/hello") {
        val handler = app()

        it("returns 200 OK with Hello, World!") {
            val response = handler(Request(GET, "/hello"))
            response.status shouldBe OK
            response.bodyString() shouldBe "Hello, World!"
        }
    }

    describe("/toggle") {
        val handler = app()

        it("returns false for an unknown toggle") {
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            response.bodyString() shouldBe "false"
        }

        it("adds a toggle and returns its enabled state") {
            handler(Request(POST, "/toggle/myFeature?enabled=true"))
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            response.bodyString() shouldBe "true"
        }

        it("adds a disabled toggle and returns false") {
            handler(Request(POST, "/toggle/myFeature?enabled=false"))
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            response.bodyString() shouldBe "false"
        }

        it("overwriting a toggle reflects the latest value") {
            handler(Request(POST, "/toggle/myFeature?enabled=true"))
            handler(Request(POST, "/toggle/myFeature?enabled=false"))
            val response = handler(Request(GET, "/toggle/myFeature"))
            response.status shouldBe OK
            response.bodyString() shouldBe "false"
        }
    }
})
