package io.hwalrus.toggle

import com.typesafe.config.ConfigFactory
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AppConfigTest : DescribeSpec({
    describe("application.conf") {
        val config = ConfigFactory.parseResources("application.conf").resolve()

        it("sets port to 10800") {
            config.getInt("app.port") shouldBe 10800
        }

        it("has no allowed-origin") {
            config.hasPath("app.allowed-origin") shouldBe false
        }
    }

    describe("application-local.conf") {
        val config = parseConfig("local")

        it("inherits port 10800 from base config") {
            config.getInt("app.port") shouldBe 10800
        }

        it("has no allowed-origin") {
            config.hasPath("app.allowed-origin") shouldBe false
        }
    }

    describe("application-production.conf") {
        it("uses ALLOWED_ORIGIN when set") {
            val config = ConfigFactory.parseString("""ALLOWED_ORIGIN = "https://example.com"""")
                .withFallback(ConfigFactory.parseResources("application-production.conf"))
                .withFallback(ConfigFactory.parseResources("application.conf"))
                .resolve()
            config.getString("app.allowed-origin") shouldBe "https://example.com"
        }

        it("has no allowed-origin when ALLOWED_ORIGIN is absent") {
            val config = parseConfig("production")
            config.hasPath("app.allowed-origin") shouldBe false
        }

        it("inherits port 10800 from base config") {
            val config = parseConfig("production")
            config.getInt("app.port") shouldBe 10800
        }
    }
})
