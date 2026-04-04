package io.hwalrus.toggle

import java.util.concurrent.atomic.AtomicReference

class InMemoryToggleStore : ToggleStore {
    private val store = AtomicReference(emptyMap<String, Boolean>())

    override fun add(name: String, enabled: Boolean) {
        store.updateAndGet { it + (name to enabled) }
    }

    override fun isEnabled(name: String): Boolean = store.get().getOrDefault(name, false)

    override fun enable(name: String): UpdateResult = update(name, true)

    override fun disable(name: String): UpdateResult = update(name, false)

    private fun update(name: String, enabled: Boolean): UpdateResult {
        val toggleExists = name in store.getAndUpdate { snapshot ->
            if (name in snapshot) snapshot + (name to enabled) else snapshot
        }
        return if (toggleExists) UpdateResult.Updated else UpdateResult.NotFound
    }
}
