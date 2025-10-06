package com.example.data.model

import org.jetbrains.exposed.dao.id.UUIDTable


object Products : UUIDTable("products") {
    val name = varchar("name", 255)
    val price = double("price")
    val categoryId = reference("category_id", Categories)
}



