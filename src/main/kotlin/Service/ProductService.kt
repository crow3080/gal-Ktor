package com.example.Service

import com.example.db.models.Product
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

class ProductService(private val collection: MongoCollection<Product>) {

    suspend fun getAllProducts(): List<Product> {
        return collection.find().toList()
    }

    suspend fun getProductById(id: String): Product? {
        return collection.find(Filters.eq("_id", id)).toList().firstOrNull()
    }

    suspend fun createProduct(product: Product): Product {
        val newProduct = product.copy(_id = ObjectId().toString())
        collection.insertOne(newProduct)
        return newProduct
    }

    suspend fun updateProduct(id: String, product: Product): Boolean {
        val updateResult = collection.updateOne(
            Filters.eq("_id", id),
            Updates.combine(
                Updates.set("name", product.name),
                Updates.set("price", product.price),
                Updates.set("description", product.description),
                Updates.set("category", product.category)
            )
        )
        return updateResult.matchedCount > 0
    }

    suspend fun deleteProduct(id: String): Boolean {
        val deleteResult = collection.deleteOne(Filters.eq("_id", id))
        return deleteResult.deletedCount > 0
    }

    fun validateProduct(product: Product): String? {
        return when {
            product.name.isBlank() -> "اسم المنتج مطلوب"
            product.price <= 0 -> "السعر يجب أن يكون أكبر من صفر"
            product.description.isBlank() -> "وصف المنتج مطلوب"
            else -> null
        }
    }
}
