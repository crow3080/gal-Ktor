package com.example.data.model

import java.util.UUID

@kotlinx.serialization.Serializable
data class Admin(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val password: String,
    val email: String
)
