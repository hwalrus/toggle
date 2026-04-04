package io.hwalrus.toggle

sealed class StoreResult {
    data object Success : StoreResult()
    data object NotFound : StoreResult()
}

interface ToggleStore {
    fun add(name: String, enabled: Boolean)
    fun isEnabled(name: String): Boolean
    fun getAll(): Map<String, Boolean>
    fun enable(name: String): StoreResult
    fun disable(name: String): StoreResult
    fun delete(name: String): StoreResult
}
