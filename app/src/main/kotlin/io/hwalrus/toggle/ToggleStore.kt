package io.hwalrus.toggle

interface ToggleStore {
    fun add(name: String, enabled: Boolean)
    fun isEnabled(name: String): Boolean
}
