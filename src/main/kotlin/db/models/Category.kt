package com.example.db.models

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val _id: String? = null,
    val nameAr: String,
    val nameEn: String,
    val imageUrl: String? = null  // ✅ أضف هنا
)