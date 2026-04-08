package io.hwalrus.toggle

sealed class StoreResult {
    data object Success : StoreResult()
    data object NotFound : StoreResult()
    data object AlreadyExists : StoreResult()
}

sealed class GroupResult {
    data object Created : GroupResult()
    data object AlreadyExists : GroupResult()
}

sealed class ToggleResult {
    data object Created : ToggleResult()
    data object AlreadyExists : ToggleResult()
    data object GroupNotFound : ToggleResult()
}

sealed class GetResult {
    data class Found(val enabled: Boolean) : GetResult()
    data object NotFound : GetResult()
}

interface ToggleStore {
    // Group operations
    fun addGroup(group: String): GroupResult
    fun renameGroup(group: String, newName: String): StoreResult
    fun deleteGroup(group: String): StoreResult
    fun getGroups(): List<String>

    // Toggle operations (all scoped by group)
    fun add(group: String, name: String, enabled: Boolean): ToggleResult
    fun get(group: String, name: String): GetResult
    fun getAll(group: String): Map<String, Boolean>?   // null if group does not exist
    fun enable(group: String, name: String): StoreResult
    fun disable(group: String, name: String): StoreResult
    fun delete(group: String, name: String): StoreResult
    fun clear()
}
