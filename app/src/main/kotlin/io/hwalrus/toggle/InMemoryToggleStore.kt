package io.hwalrus.toggle

import java.util.concurrent.atomic.AtomicReference

class InMemoryToggleStore : ToggleStore {
    private val store = AtomicReference(emptyMap<String, Boolean>())

    override fun add(name: String, enabled: Boolean) {
        store.updateAndGet { it + (name to enabled) }
    }

    override fun get(name: String): GetResult {
        val snapshot = store.get()
        return if (name in snapshot) GetResult.Found(snapshot.getValue(name)) else GetResult.NotFound
    }

    override fun getAll(): Map<String, Boolean> = store.get()

    override fun enable(name: String): StoreResult = update(name, true)

    override fun disable(name: String): StoreResult = update(name, false)

    override fun delete(name: String): StoreResult {
        val toggleExists = name in store.getAndUpdate { snapshot -> snapshot - name }
        return if (toggleExists) StoreResult.Success else StoreResult.NotFound
    }

    private fun update(name: String, enabled: Boolean): StoreResult {
        val toggleExists = name in store.getAndUpdate { snapshot ->
            if (name in snapshot) snapshot + (name to enabled) else snapshot
        }
        return if (toggleExists) StoreResult.Success else StoreResult.NotFound
    }
}
