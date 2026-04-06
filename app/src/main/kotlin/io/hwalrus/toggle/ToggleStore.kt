package io.hwalrus.toggle

sealed class StoreResult {
    data object Success : StoreResult()
    data object NotFound : StoreResult()
}

sealed class GetResult {
    data class Found(val enabled: Boolean) : GetResult()
    data object NotFound : GetResult()
}

interface ToggleStore {
    fun add(name: String, enabled: Boolean)
    fun get(name: String): GetResult
    fun getAll(): Map<String, Boolean>
    fun enable(name: String): StoreResult
    fun disable(name: String): StoreResult
    fun delete(name: String): StoreResult
    fun clear()
}
