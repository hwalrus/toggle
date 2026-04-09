package io.hwalrus.toggle

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

internal fun parseConfig(env: String): Config =
    ConfigFactory.parseResources("application-$env.conf")
        .withFallback(ConfigFactory.parseResources("application.conf"))
        .resolve()

internal object AppConfig {
    private val config = parseConfig(System.getenv("APP_ENV") ?: "local")

    val port: Int = config.getInt("app.port")
    val store: String = config.getString("app.store")
    val mongoUri: String? =
        if (config.hasPath("app.mongodb.uri")) config.getString("app.mongodb.uri").ifBlank { null }
        else null
    val allowedOrigin: String? =
        if (config.hasPath("app.allowed-origin")) config.getString("app.allowed-origin").ifBlank { null }
        else null
}
