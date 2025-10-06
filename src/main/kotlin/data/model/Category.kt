package com.example.data.model

import org.jetbrains.exposed.dao.id.UUIDTable

object Categories : UUIDTable("categories") {
    val name = varchar("name", 255).uniqueIndex()
}