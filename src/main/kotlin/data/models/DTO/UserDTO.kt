package com.example.data.models.DTO

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val name: String,
    val email: String,
    val age: Int,
    val photo: String? = null // Base64 encoded photo or file path
)

@Serializable
data class CategoryDTO(
    val name: String,
    val photo: String? = null
)