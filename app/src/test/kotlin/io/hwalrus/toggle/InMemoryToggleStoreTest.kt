package io.hwalrus.toggle

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class InMemoryToggleStoreTest : DescribeSpec({
    describe("InMemoryToggleStore") {
        describe("addGroup") {
            it("returns Created on first add") {
                InMemoryToggleStore().addGroup("g") shouldBe GroupResult.Created
            }

            it("returns AlreadyExists when group already present") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.addGroup("g") shouldBe GroupResult.AlreadyExists
            }
        }

        describe("getGroups") {
            it("returns empty list when no groups exist") {
                InMemoryToggleStore().getGroups() shouldBe emptyList()
            }

            it("returns sorted group names") {
                val store = InMemoryToggleStore()
                store.addGroup("beta")
                store.addGroup("alpha")
                store.getGroups() shouldBe listOf("alpha", "beta")
            }
        }

        describe("renameGroup") {
            it("returns NotFound for an unknown group") {
                InMemoryToggleStore().renameGroup("unknown", "new") shouldBe StoreResult.NotFound
            }

            it("renames the group and preserves its toggles") {
                val store = InMemoryToggleStore()
                store.addGroup("old")
                store.add("old", "feat", true)
                store.renameGroup("old", "new") shouldBe StoreResult.Success
                store.getGroups() shouldBe listOf("new")
                store.get("new", "feat") shouldBe GetResult.Found(true)
                store.get("old", "feat") shouldBe GetResult.NotFound
            }

            it("returns AlreadyExists when the target name is already taken") {
                val store = InMemoryToggleStore()
                store.addGroup("a")
                store.addGroup("b")
                store.add("b", "existing-toggle", true)
                store.renameGroup("a", "b") shouldBe StoreResult.AlreadyExists
                store.getGroups() shouldBe listOf("a", "b")
                store.get("b", "existing-toggle") shouldBe GetResult.Found(true)
            }

            it("renaming a group to its own name is a no-op success") {
                val store = InMemoryToggleStore()
                store.addGroup("a")
                store.renameGroup("a", "a") shouldBe StoreResult.Success
                store.getGroups() shouldBe listOf("a")
            }
        }

        describe("deleteGroup") {
            it("returns NotFound for an unknown group") {
                InMemoryToggleStore().deleteGroup("unknown") shouldBe StoreResult.NotFound
            }

            it("deletes the group and its toggles") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "feat", true)
                store.deleteGroup("g") shouldBe StoreResult.Success
                store.getGroups() shouldBe emptyList()
                store.get("g", "feat") shouldBe GetResult.NotFound
            }
        }

        describe("getAll") {
            it("returns null for an unknown group") {
                InMemoryToggleStore().getAll("unknown") shouldBe null
            }

            it("returns empty map for a group with no toggles") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.getAll("g") shouldBe emptyMap()
            }

            it("returns all toggles in the group") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "a", true)
                store.add("g", "b", false)
                store.getAll("g") shouldBe mapOf("a" to true, "b" to false)
            }

            it("does not include deleted toggles") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "a", true)
                store.add("g", "b", false)
                store.delete("g", "a")
                store.getAll("g") shouldBe mapOf("b" to false)
            }

            it("groups are independent") {
                val store = InMemoryToggleStore()
                store.addGroup("g1")
                store.addGroup("g2")
                store.add("g1", "feat", true)
                store.getAll("g1") shouldBe mapOf("feat" to true)
                store.getAll("g2") shouldBe emptyMap()
            }
        }

        describe("get") {
            it("returns NotFound for an unknown group") {
                InMemoryToggleStore().get("unknown", "feat") shouldBe GetResult.NotFound
            }

            it("returns NotFound for an unknown toggle within a group") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.get("g", "unknown") shouldBe GetResult.NotFound
            }

            it("returns Found with enabled=true") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "feat", true)
                store.get("g", "feat") shouldBe GetResult.Found(true)
            }

            it("returns Found with enabled=false") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "feat", false)
                store.get("g", "feat") shouldBe GetResult.Found(false)
            }
        }

        describe("add") {
            it("returns GroupNotFound when the group does not exist") {
                InMemoryToggleStore().add("unknown", "feat", true) shouldBe ToggleResult.GroupNotFound
            }

            it("adds a toggle and returns Created") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "feat", true) shouldBe ToggleResult.Created
                store.get("g", "feat") shouldBe GetResult.Found(true)
            }

            it("same name can exist in different groups") {
                val store = InMemoryToggleStore()
                store.addGroup("g1")
                store.addGroup("g2")
                store.add("g1", "feat", true)
                store.add("g2", "feat", false)
                store.get("g1", "feat") shouldBe GetResult.Found(true)
                store.get("g2", "feat") shouldBe GetResult.Found(false)
            }

            it("returns AlreadyExists when toggle name is taken in the group") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "feat", true)
                store.add("g", "feat", false) shouldBe ToggleResult.AlreadyExists
                store.get("g", "feat") shouldBe GetResult.Found(true)
            }
        }

        describe("enable") {
            it("returns NotFound for an unknown group") {
                InMemoryToggleStore().enable("unknown", "feat") shouldBe StoreResult.NotFound
            }

            it("returns NotFound for an unknown toggle") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.enable("g", "unknown") shouldBe StoreResult.NotFound
            }

            it("enables a disabled toggle") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "feat", false)
                store.enable("g", "feat") shouldBe StoreResult.Success
                store.get("g", "feat") shouldBe GetResult.Found(true)
            }
        }

        describe("disable") {
            it("returns NotFound for an unknown group") {
                InMemoryToggleStore().disable("unknown", "feat") shouldBe StoreResult.NotFound
            }

            it("returns NotFound for an unknown toggle") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.disable("g", "unknown") shouldBe StoreResult.NotFound
            }

            it("disables an enabled toggle") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "feat", true)
                store.disable("g", "feat") shouldBe StoreResult.Success
                store.get("g", "feat") shouldBe GetResult.Found(false)
            }
        }

        describe("delete") {
            it("returns NotFound for an unknown group") {
                InMemoryToggleStore().delete("unknown", "feat") shouldBe StoreResult.NotFound
            }

            it("returns NotFound for an unknown toggle") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.delete("g", "unknown") shouldBe StoreResult.NotFound
            }

            it("deletes an existing toggle") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "feat", true)
                store.delete("g", "feat") shouldBe StoreResult.Success
                store.get("g", "feat") shouldBe GetResult.NotFound
            }

            it("deleting one toggle does not affect others") {
                val store = InMemoryToggleStore()
                store.addGroup("g")
                store.add("g", "a", true)
                store.add("g", "b", true)
                store.delete("g", "a")
                store.get("g", "b") shouldBe GetResult.Found(true)
            }
        }
    }
})
