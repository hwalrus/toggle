package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class MongoToggleStoreTest : DescribeSpec({
    val container = autoClose(MongoDBContainer(DockerImageName.parse("mongo:8")).also { it.start() })
    toggleStoreContract(MongoToggleStore(container.connectionString))
})
