package com.example.data.model

import java.util.UUID


@kotlinx.serialization.Serializable
data class Cart(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val items: List<CartItem> = emptyList()
)