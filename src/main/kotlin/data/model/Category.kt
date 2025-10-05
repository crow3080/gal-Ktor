package com.example.data.model

import java.util.UUID

@kotlinx.serialization.Serializable
data class Category(val id: String = UUID.randomUUID().toString(), val name: String)