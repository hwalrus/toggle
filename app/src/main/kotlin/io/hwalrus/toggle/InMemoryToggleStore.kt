package io.hwalrus.toggle

import java.util.concurrent.atomic.AtomicReference

class InMemoryToggleStore : ToggleStore {
    private val store = AtomicReference(emptyMap<String, Map<String, Boolean>>())

    override fun addGroup(group: String): GroupResult {
        while (true) {
            val current = store.get()
            if (group in current) return GroupResult.AlreadyExists
            if (store.compareAndSet(current, current + (group to emptyMap()))) return GroupResult.Created
        }
    }

    override fun renameGroup(group: String, newName: String): StoreResult {
        while (true) {
            val current = store.get()
            if (group !in current) return StoreResult.NotFound
            if (newName in current && newName != group) return StoreResult.AlreadyExists
            if (store.compareAndSet(current, (current - group) + (newName to current.getValue(group)))) return StoreResult.Success
        }
    }

    override fun deleteGroup(group: String): StoreResult {
        while (true) {
            val current = store.get()
            if (group !in current) return StoreResult.NotFound
            if (store.compareAndSet(current, current - group)) return StoreResult.Success
        }
    }

    override fun getGroups(): List<String> = store.get().keys.sorted()

    override fun add(group: String, name: String, enabled: Boolean): ToggleResult {
        while (true) {
            val current = store.get()
            if (group !in current) return ToggleResult.GroupNotFound
            val toggles = current.getValue(group)
            if (name in toggles) return ToggleResult.AlreadyExists
            if (store.compareAndSet(current, current + (group to (toggles + (name to enabled))))) return ToggleResult.Created
        }
    }

    override fun get(group: String, name: String): GetResult {
        val toggles = store.get()[group] ?: return GetResult.NotFound
        return if (name in toggles) GetResult.Found(toggles.getValue(name)) else GetResult.NotFound
    }

    override fun getAll(group: String): Map<String, Boolean>? = store.get()[group]

    override fun enable(group: String, name: String): StoreResult = update(group, name, true)

    override fun disable(group: String, name: String): StoreResult = update(group, name, false)

    override fun delete(group: String, name: String): StoreResult {
        while (true) {
            val current = store.get()
            val toggles = current[group] ?: return StoreResult.NotFound
            if (name !in toggles) return StoreResult.NotFound
            if (store.compareAndSet(current, current + (group to (toggles - name)))) return StoreResult.Success
        }
    }

    override fun clear() {
        store.set(emptyMap())
    }

    private fun update(group: String, name: String, enabled: Boolean): StoreResult {
        while (true) {
            val current = store.get()
            val toggles = current[group] ?: return StoreResult.NotFound
            if (name !in toggles) return StoreResult.NotFound
            if (store.compareAndSet(current, current + (group to (toggles + (name to enabled))))) return StoreResult.Success
        }
    }
}
