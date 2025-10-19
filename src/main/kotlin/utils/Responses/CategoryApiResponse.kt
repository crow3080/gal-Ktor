package com.example.utils.Responses

import com.example.db.models.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryApiResponse(
    val success: Boolean,
    val message: String,
    val data: Category? = null
)