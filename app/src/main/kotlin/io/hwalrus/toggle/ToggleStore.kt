package io.hwalrus.toggle

sealed class UpdateResult {
    data object Updated : UpdateResult()
    data object NotFound : UpdateResult()
}

interface ToggleStore {
    fun add(name: String, enabled: Boolean)
    fun isEnabled(name: String): Boolean
    fun enable(name: String): UpdateResult
    fun disable(name: String): UpdateResult
    fun delete(name: String): UpdateResult
}
