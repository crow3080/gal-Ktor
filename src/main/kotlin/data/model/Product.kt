package com.example.data.model

import java.util.UUID

@kotlinx.serialization.Serializable
data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val categoryId: String
)
