package com.example.db

import com.example.db.models.Category
import com.example.db.models.ContactMessage
import com.example.db.models.Product
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase

object DatabaseConfig {
    private const val MONGO_STRING = "mongodb://hassan:54615@109.123.251.232:27017/?authSource=admin"
    private const val DATABASE_NAME = "Gal_db"

    private val client: MongoClient by lazy {
        MongoClient.create(MONGO_STRING)
    }

    private val database: MongoDatabase by lazy {
        client.getDatabase(DATABASE_NAME)
    }

    val productCollection: MongoCollection<Product> by lazy {
        database.getCollection<Product>("products")
    }

    val categoryCollection: MongoCollection<Category> by lazy {
        database.getCollection<Category>("categories")
    }

    val contactMessageCollection: MongoCollection<ContactMessage> by lazy {
        database.getCollection<ContactMessage>("contact_messages")
    }

    fun closeConnection() {
        client.close()
    }
}