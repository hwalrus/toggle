package io.hwalrus.toggle

import java.util.concurrent.atomic.AtomicReference

class InMemoryToggleStore : ToggleStore {
    private val store = AtomicReference(emptyMap<String, Boolean>())

    override fun add(name: String, enabled: Boolean) {
        store.updateAndGet { it + (name to enabled) }
    }

    override fun isEnabled(name: String): Boolean = store.get().getOrDefault(name, false)
}
