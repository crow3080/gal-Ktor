package com.example.Service

import com.example.db.models.Category
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class CategoryService(private val collection: MongoCollection<Category>) {

    suspend fun getAllCategories(): List<Category> {
        return collection.find().toList()
    }

    suspend fun getCategoryById(id: String): Category? {
        return collection.find(Filters.eq("_id", id)).toList().firstOrNull()
    }

    suspend fun createCategory(category: Category): Category {
        val newCategory = category.copy(_id = ObjectId().toString())
        collection.insertOne(newCategory)
        return newCategory
    }

    suspend fun updateCategory(id: String, category: Category): Boolean {
        val updateResult = collection.updateOne(
            Filters.eq("_id", id),
            Updates.combine(
                Updates.set("nameAr", category.nameAr),
                Updates.set("nameEn", category.nameEn),
                Updates.set("imageUrl", category.imageUrl)  // ✅
            )
        )
        return updateResult.matchedCount > 0
    }

    suspend fun deleteCategory(id: String): Boolean {
        val deleteResult = collection.deleteOne(Filters.eq("_id", id))
        return deleteResult.deletedCount > 0
    }
}