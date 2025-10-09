package com.example.db.models

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val _id: String? = null,
    val name: String,
    val price: Double,
    val description: String,
    val category: String = "عام"
)