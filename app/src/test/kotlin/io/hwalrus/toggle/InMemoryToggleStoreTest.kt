package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class InMemoryToggleStoreTest : DescribeSpec({
    describe("InMemoryToggleStore") {
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
