package com.example.Service

import com.example.db.DatabaseConfig
import com.example.db.models.Product
import com.example.utils.Responses.PaginatedResponse
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import org.bson.types.ObjectId

class ProductService(private val collection: MongoCollection<Product>) {

    suspend fun getAllProducts(): List<Product> {
        return collection.find().toList()
    }

    suspend fun getProductsPaginated(page: Int, limit: Int, search: String, category: String? = null, sort: String? = null): PaginatedResponse<Product> {
        val skip = (page - 1) * limit

        val filters = mutableListOf<Bson>()

        if (search.isNotEmpty()) {
            val cleanSearch = search.trim()

            if (cleanSearch.isNotEmpty()) {
                // استخدام البحث النصي مع الفهرس (يشمل nameAr, nameEn, etc.)
                filters.add(Filters.text(cleanSearch))
            }
        }

        // فلتر التصنيف
        if (!category.isNullOrEmpty()) {
            filters.add(Filters.eq("categoryId", category))
        }

        val filter = if (filters.isNotEmpty()) {
            Filters.and(filters)
        } else {
            Filters.empty()
        }

        // بناء options الترتيب
        val sortOptions = when(sort) {
            "price-asc" -> Sorts.ascending("price")
            "price-desc" -> Sorts.descending("price")
            "name" -> Sorts.ascending("name")
            else -> {
                // إذا كان هناك بحث، رتب حسب النقاط النصية أولاً
                if (search.isNotEmpty()) {
                    Sorts.metaTextScore("score")
                } else {
                    Sorts.descending("createdAt")
                }
            }
        }

        // جلب المنتجات
        val findOperation = collection.find(filter)
            .sort(sortOptions)
            .skip(skip)
            .limit(limit)

        // إذا كان هناك بحث، أضف إسقاط النقاط النصية
        val products = if (search.isNotEmpty()) {
            findOperation.projection(Projections.metaTextScore("score")).toList()
        } else {
            findOperation.toList()
        }

        // حساب إجمالي العدد
        val totalCount = collection.countDocuments(filter)

        return PaginatedResponse(
            data = products,
            page = page,
            limit = limit,
            total = totalCount,
            totalPages = ((totalCount + limit - 1) / limit).toInt()
        )
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
                Updates.set("nameAr", product.nameAr),
                Updates.set("nameEn", product.nameEn),
                Updates.set("price", product.price),
                Updates.set("descriptionAr", product.descriptionAr),
                Updates.set("descriptionEn", product.descriptionEn),
                Updates.set("categoryId", product.categoryId),
                Updates.set("imageUrl", product.imageUrl)
            )
        )
        return updateResult.matchedCount > 0
    }

    suspend fun categoryExists(categoryId: String): Boolean {
        val categoryCollection = DatabaseConfig.categoryCollection
        val found = categoryCollection.find(Filters.eq("_id", categoryId)).toList()
        return found.isNotEmpty()
    }

    suspend fun deleteProduct(id: String): Boolean {
        val deleteResult = collection.deleteOne(Filters.eq("_id", id))
        return deleteResult.deletedCount > 0
    }

    fun validateProduct(product: Product): String? {
        return when {
            product.nameAr.isBlank() -> "اسم المنتج بالعربية مطلوب"
            product.price <= 0 -> "السعر يجب أن يكون أكبر من صفر"
            product.descriptionAr.isBlank() -> "وصف المنتج بالعربية مطلوب"
            product.categoryId.isBlank() -> "يجب اختيار تصنيف للمنتج"
            else -> null
        }
    }
}