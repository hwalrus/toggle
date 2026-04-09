package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import org.testcontainers.mongodb.MongoDBContainer

class MongoToggleStoreTest : DescribeSpec({
    val container = autoClose(MongoDBContainer("mongo:8").also { it.start() })
    toggleStoreContract(MongoToggleStore(container.connectionString))
})
