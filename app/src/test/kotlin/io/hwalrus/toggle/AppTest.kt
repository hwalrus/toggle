package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK

class AppTest : DescribeSpec({
    describe("/hello") {
        it("returns 200 OK with Hello, World!") {
            val response = app(Request(GET, "/hello"))
            response.status shouldBe OK
            response.bodyString() shouldBe "Hello, World!"
        }
    }
})
