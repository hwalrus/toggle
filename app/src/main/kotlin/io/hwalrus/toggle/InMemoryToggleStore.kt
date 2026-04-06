package io.hwalrus.toggle

import java.util.concurrent.atomic.AtomicReference

class InMemoryToggleStore : ToggleStore {
    private val store = AtomicReference(emptyMap<String, Map<String, Boolean>>())

    override fun addGroup(group: String): GroupResult {
        var result: GroupResult = GroupResult.AlreadyExists
        store.updateAndGet { current ->
            if (group in current) {
                current
            } else {
                result = GroupResult.Created
                current + (group to emptyMap())
            }
        }
        return result
    }

    override fun renameGroup(group: String, newName: String): StoreResult {
        var result: StoreResult = StoreResult.NotFound
        store.updateAndGet { current ->
            if (group !in current) {
                current
            } else {
                result = StoreResult.Success
                (current - group) + (newName to current.getValue(group))
            }
        }
        return result
    }

    override fun deleteGroup(group: String): StoreResult {
        var existed = false
        store.updateAndGet { current ->
            if (group in current) {
                existed = true
                current - group
            } else {
                current
            }
        }
        return if (existed) StoreResult.Success else StoreResult.NotFound
    }

    override fun getGroups(): List<String> = store.get().keys.sorted()

    override fun add(group: String, name: String, enabled: Boolean): StoreResult {
        var result: StoreResult = StoreResult.NotFound
        store.updateAndGet { current ->
            if (group !in current) {
                current
            } else {
                result = StoreResult.Success
                current + (group to (current.getValue(group) + (name to enabled)))
            }
        }
        return result
    }

    override fun get(group: String, name: String): GetResult {
        val toggles = store.get()[group] ?: return GetResult.NotFound
        return if (name in toggles) GetResult.Found(toggles.getValue(name)) else GetResult.NotFound
    }

    override fun getAll(group: String): Map<String, Boolean>? = store.get()[group]

    override fun enable(group: String, name: String): StoreResult = update(group, name, true)

    override fun disable(group: String, name: String): StoreResult = update(group, name, false)

    override fun delete(group: String, name: String): StoreResult {
        var existed = false
        store.updateAndGet { current ->
            val toggles = current[group] ?: return@updateAndGet current
            if (name !in toggles) {
                current
            } else {
                existed = true
                current + (group to (toggles - name))
            }
        }
        return if (existed) StoreResult.Success else StoreResult.NotFound
    }

    override fun clear() {
        store.set(emptyMap())
    }

    private fun update(group: String, name: String, enabled: Boolean): StoreResult {
        var existed = false
        store.updateAndGet { current ->
            val toggles = current[group] ?: return@updateAndGet current
            if (name !in toggles) {
                current
            } else {
                existed = true
                current + (group to (toggles + (name to enabled)))
            }
        }
        return if (existed) StoreResult.Success else StoreResult.NotFound
    }
}
