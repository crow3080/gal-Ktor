package com.example.utils.Responses

import com.example.db.models.Product
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Product? = null
)

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val page: Int,
    val limit: Int,
    val total: Long,
    val totalPages: Int
)