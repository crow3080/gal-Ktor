package com.example.utils.Responses

import com.example.db.models.Product
import kotlinx.serialization.Serializable

@Serializable
data class ImportResponse(
    val success: Boolean,
    val message: String,
    val data: List<Product>
)