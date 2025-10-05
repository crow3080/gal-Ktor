package com.example.data.model

@kotlinx.serialization.Serializable
data class CartItem(val productId: String, val quantity: Int)