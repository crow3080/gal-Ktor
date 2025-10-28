package com.example.db.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import org.bson.types.ObjectId

@Serializable
data class ContactMessage(
    @Contextual val _id: ObjectId? = null,
    val name: String,
    val email: String,
    val phone: String? = null,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)