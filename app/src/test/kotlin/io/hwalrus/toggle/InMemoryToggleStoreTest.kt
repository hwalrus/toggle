package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class InMemoryToggleStoreTest : DescribeSpec({
    describe("InMemoryToggleStore") {
        describe("getAll") {
            it("returns an empty map when no toggles exist") {
                InMemoryToggleStore().getAll() shouldBe emptyMap()
            }

            it("returns all added toggles") {
                val store = InMemoryToggleStore()
                store.add("a", true)
                store.add("b", false)
                store.getAll() shouldBe mapOf("a" to true, "b" to false)
            }

            it("reflects the latest state after updates") {
                val store = InMemoryToggleStore()
                store.add("a", true)
                store.disable("a")
                store.getAll() shouldBe mapOf("a" to false)
            }

            it("does not include deleted toggles") {
                val store = InMemoryToggleStore()
                store.add("a", true)
                store.add("b", false)
                store.delete("a")
                store.getAll() shouldBe mapOf("b" to false)
            }
        }

        describe("isEnabled") {
            it("returns false for an unknown toggle") {
                InMemoryToggleStore().isEnabled("unknown") shouldBe false
            }
        }

        describe("add") {
            it("stores an enabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.isEnabled("feature") shouldBe true
            }

            it("stores a disabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.isEnabled("feature") shouldBe false
            }

            it("overwrites an enabled toggle with disabled") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.add("feature", false)
                store.isEnabled("feature") shouldBe false
            }

            it("overwrites a disabled toggle with enabled") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.add("feature", true)
                store.isEnabled("feature") shouldBe true
            }

            it("toggles are independent of each other") {
                val store = InMemoryToggleStore()
                store.add("a", true)
                store.add("b", false)
                store.isEnabled("a") shouldBe true
                store.isEnabled("b") shouldBe false
            }
        }

        describe("enable") {
            it("returns NotFound for an unknown toggle") {
                InMemoryToggleStore().enable("unknown") shouldBe UpdateResult.NotFound
            }

            it("returns Updated and enables a disabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.enable("feature") shouldBe UpdateResult.Updated
                store.isEnabled("feature") shouldBe true
            }

            it("returns Updated when toggle is already enabled") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.enable("feature") shouldBe UpdateResult.Updated
                store.isEnabled("feature") shouldBe true
            }
        }

        describe("delete") {
            it("returns NotFound for an unknown toggle") {
                InMemoryToggleStore().delete("unknown") shouldBe UpdateResult.NotFound
            }

            it("returns Updated and removes an existing toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.delete("feature") shouldBe UpdateResult.Updated
                store.isEnabled("feature") shouldBe false
            }

            it("toggle is no longer accessible after deletion") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.delete("feature")
                store.delete("feature") shouldBe UpdateResult.NotFound
            }

            it("deleting one toggle does not affect others") {
                val store = InMemoryToggleStore()
                store.add("a", true)
                store.add("b", true)
                store.delete("a")
                store.isEnabled("b") shouldBe true
            }
        }

        describe("disable") {
            it("returns NotFound for an unknown toggle") {
                InMemoryToggleStore().disable("unknown") shouldBe UpdateResult.NotFound
            }

            it("returns Updated and disables an enabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.disable("feature") shouldBe UpdateResult.Updated
                store.isEnabled("feature") shouldBe false
            }

            it("returns Updated when toggle is already disabled") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.disable("feature") shouldBe UpdateResult.Updated
                store.isEnabled("feature") shouldBe false
            }
        }
    }
})
