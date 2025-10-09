package com.example.utils

import com.example.db.models.Product
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Product? = null
)