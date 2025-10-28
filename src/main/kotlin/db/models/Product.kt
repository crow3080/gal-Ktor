package com.example.db.models

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val _id: String? = null,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val price: Double,
    val categoryId: String,
    val imageUrl: String? = null
)