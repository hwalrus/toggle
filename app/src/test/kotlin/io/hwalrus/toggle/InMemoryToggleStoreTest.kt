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

        describe("get") {
            it("returns NotFound for an unknown toggle") {
                InMemoryToggleStore().get("unknown") shouldBe GetResult.NotFound
            }

            it("returns Found with enabled=true for an enabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.get("feature") shouldBe GetResult.Found(true)
            }

            it("returns Found with enabled=false for a disabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.get("feature") shouldBe GetResult.Found(false)
            }
        }

        describe("add") {
            it("stores an enabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.get("feature") shouldBe GetResult.Found(true)
            }

            it("stores a disabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.get("feature") shouldBe GetResult.Found(false)
            }

            it("overwrites an enabled toggle with disabled") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.add("feature", false)
                store.get("feature") shouldBe GetResult.Found(false)
            }

            it("overwrites a disabled toggle with enabled") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.add("feature", true)
                store.get("feature") shouldBe GetResult.Found(true)
            }

            it("toggles are independent of each other") {
                val store = InMemoryToggleStore()
                store.add("a", true)
                store.add("b", false)
                store.get("a") shouldBe GetResult.Found(true)
                store.get("b") shouldBe GetResult.Found(false)
            }
        }

        describe("enable") {
            it("returns NotFound for an unknown toggle") {
                InMemoryToggleStore().enable("unknown") shouldBe StoreResult.NotFound
            }

            it("returns Success and enables a disabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.enable("feature") shouldBe StoreResult.Success
                store.get("feature") shouldBe GetResult.Found(true)
            }

            it("returns Success when toggle is already enabled") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.enable("feature") shouldBe StoreResult.Success
                store.get("feature") shouldBe GetResult.Found(true)
            }
        }

        describe("disable") {
            it("returns NotFound for an unknown toggle") {
                InMemoryToggleStore().disable("unknown") shouldBe StoreResult.NotFound
            }

            it("returns Success and disables an enabled toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.disable("feature") shouldBe StoreResult.Success
                store.get("feature") shouldBe GetResult.Found(false)
            }

            it("returns Success when toggle is already disabled") {
                val store = InMemoryToggleStore()
                store.add("feature", false)
                store.disable("feature") shouldBe StoreResult.Success
                store.get("feature") shouldBe GetResult.Found(false)
            }
        }

        describe("delete") {
            it("returns NotFound for an unknown toggle") {
                InMemoryToggleStore().delete("unknown") shouldBe StoreResult.NotFound
            }

            it("returns Success and removes an existing toggle") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.delete("feature") shouldBe StoreResult.Success
                store.get("feature") shouldBe GetResult.NotFound
            }

            it("toggle is no longer accessible after deletion") {
                val store = InMemoryToggleStore()
                store.add("feature", true)
                store.delete("feature")
                store.delete("feature") shouldBe StoreResult.NotFound
            }

            it("deleting one toggle does not affect others") {
                val store = InMemoryToggleStore()
                store.add("a", true)
                store.add("b", true)
                store.delete("a")
                store.get("b") shouldBe GetResult.Found(true)
            }
        }
    }
})
