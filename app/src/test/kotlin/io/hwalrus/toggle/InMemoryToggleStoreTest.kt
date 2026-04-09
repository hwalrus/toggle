package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec

class InMemoryToggleStoreTest : DescribeSpec({
    toggleStoreContract(InMemoryToggleStore())
})
