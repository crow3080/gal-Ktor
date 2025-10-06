package com.example.data.driver


import com.example.data.model.Categories
import com.example.data.model.Products
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


fun initDatabase() {
    Database.connect("jdbc:sqlite:data.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(Categories, Products)
    }
}