package com.example.data.model.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(val id: String? = null, val name: String, val price: Double, val categoryId: String)
