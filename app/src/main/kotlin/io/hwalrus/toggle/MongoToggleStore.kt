package io.hwalrus.toggle

import com.mongodb.MongoWriteException
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.exists
import com.mongodb.client.model.Updates.set
import com.mongodb.client.model.Updates.unset
import com.mongodb.kotlin.client.MongoClient
import org.bson.Document

private const val DUPLICATE_KEY = 11000

class MongoToggleStore(connectionString: String) : ToggleStore {
    private val client = MongoClient.create(connectionString)
    private val collection = client.getDatabase("toggle").getCollection<Document>("toggles")

    override fun addGroup(group: String): GroupResult = try {
        collection.insertOne(Document("_id", group).append("toggles", Document()))
        GroupResult.Created
    } catch (e: MongoWriteException) {
        if (e.error.code != DUPLICATE_KEY) throw e
        GroupResult.AlreadyExists
    }

    override fun renameGroup(group: String, newName: String): StoreResult {
        if (group == newName) {
            return if (collection.countDocuments(eq("_id", group)) > 0L) StoreResult.Success
            else StoreResult.NotFound
        }
        val doc = findGroupDoc(group) ?: return StoreResult.NotFound
        return try {
            collection.insertOne(Document("_id", newName).append("toggles", doc["toggles"]))
            collection.deleteOne(eq("_id", group))
            StoreResult.Success
        } catch (e: MongoWriteException) {
            if (e.error.code != DUPLICATE_KEY) throw e
            StoreResult.AlreadyExists
        }
    }

    override fun deleteGroup(group: String): StoreResult {
        val result = collection.deleteOne(eq("_id", group))
        return if (result.deletedCount > 0L) StoreResult.Success else StoreResult.NotFound
    }

    override fun getGroups(): List<String> =
        collection.find().cursor().use { cursor ->
            buildList { while (cursor.hasNext()) cursor.next().getString("_id")?.let { add(it) } }
        }.sorted()

    override fun add(group: String, name: String, enabled: Boolean): ToggleResult {
        val result = collection.updateOne(
            and(eq("_id", group), exists("toggles.$name", false)),
            set("toggles.$name", enabled)
        )
        if (result.matchedCount > 0L) return ToggleResult.Created
        return if (collection.countDocuments(eq("_id", group)) > 0L) ToggleResult.AlreadyExists
        else ToggleResult.GroupNotFound
    }

    override fun get(group: String, name: String): GetResult {
        val toggles = findGroupDoc(group)?.get("toggles", Document::class.java)
            ?: return GetResult.NotFound
        val value = toggles[name] ?: return GetResult.NotFound
        return GetResult.Found(value as Boolean)
    }

    override fun getAll(group: String): Map<String, Boolean>? {
        val doc = findGroupDoc(group) ?: return null
        val toggles = doc.get("toggles", Document::class.java) ?: return emptyMap()
        return toggles.entries.associate { it.key to it.value as Boolean }
    }

    override fun enable(group: String, name: String): StoreResult = setState(group, name, true)

    override fun disable(group: String, name: String): StoreResult = setState(group, name, false)

    override fun delete(group: String, name: String): StoreResult {
        val result = collection.updateOne(
            and(eq("_id", group), exists("toggles.$name")),
            unset("toggles.$name")
        )
        return if (result.matchedCount > 0L) StoreResult.Success else StoreResult.NotFound
    }

    override fun clear() {
        collection.deleteMany(Document())
    }

    private fun findGroupDoc(group: String): Document? =
        collection.find(eq("_id", group)).limit(1).cursor().use { if (it.hasNext()) it.next() else null }

    private fun setState(group: String, name: String, enabled: Boolean): StoreResult {
        val result = collection.updateOne(
            and(eq("_id", group), exists("toggles.$name")),
            set("toggles.$name", enabled)
        )
        return if (result.matchedCount > 0L) StoreResult.Success else StoreResult.NotFound
    }
}
