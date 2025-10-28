package com.example.Service

import com.example.db.models.ContactMessage
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class ContactMessageService(
    private val collection: MongoCollection<ContactMessage>
) {

    // إضافة رسالة جديدة
    suspend fun createMessage(message: ContactMessage): ContactMessage {
        collection.insertOne(message)
        return message
    }

    // الحصول على جميع الرسائل مع إمكانية الفلترة
    suspend fun getAllMessages(
        page: Int = 1,
        limit: Int = 20,
        readFilter: Boolean? = null
    ): List<ContactMessage> {
        val skip = (page - 1) * limit

        val filter = readFilter?.let {
            Filters.eq("isRead", it)
        } ?: Filters.empty()

        return collection
            .find(filter)
            .sort(Sorts.descending("createdAt"))
            .skip(skip)
            .limit(limit)
            .toList()
    }

    // الحصول على عدد الرسائل
    suspend fun getMessagesCount(readFilter: Boolean? = null): Long {
        val filter = readFilter?.let {
            Filters.eq("isRead", it)
        } ?: Filters.empty()

        return collection.countDocuments(filter)
    }

    // تحديث حالة القراءة
    suspend fun markAsRead(id: String, isRead: Boolean): Boolean {
        // Validate ObjectId
        if (!ObjectId.isValid(id)) {
            println("❌ Invalid ObjectId: $id")
            return false
        }
        val objectId = try {
            ObjectId(id)
        } catch (e: IllegalArgumentException) {
            println("❌ Failed to parse ObjectId: ${e.message}")
            return false
        }
        val result = collection.updateOne(
            Filters.eq("_id", objectId),
            Updates.set(ContactMessage::isRead.name, isRead)
        )
        return result.modifiedCount > 0
    }

    // حذف رسالة
    suspend fun deleteMessage(id: String): Boolean {
        val result = collection.deleteOne(
            Filters.eq("_id", ObjectId(id))
        )
        return result.deletedCount > 0
    }

    // البحث في الرسائل
    suspend fun searchMessages(query: String): List<ContactMessage> {
        val searchFilter = Filters.or(
            Filters.regex("name", query, "i"),
            Filters.regex("email", query, "i"),
            Filters.regex("message", query, "i")
        )

        return collection
            .find(searchFilter)
            .sort(Sorts.descending("createdAt"))
            .toList()
    }
}