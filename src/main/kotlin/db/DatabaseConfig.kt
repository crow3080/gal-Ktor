package com.example.db

import com.example.db.models.Product
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase

object DatabaseConfig {
    private const val MONGO_STRING = "mongodb://localhost:27017/"
    private const val DATABASE_NAME = "Gal_db"
    private const val COLLECTION_NAME = "products"

    private val client: MongoClient by lazy {
        MongoClient.create(MONGO_STRING)
    }

    private val database: MongoDatabase by lazy {
        client.getDatabase(DATABASE_NAME)
    }

    val productCollection: MongoCollection<Product> by lazy {
        database.getCollection<Product>(COLLECTION_NAME)
    }

    fun closeConnection() {
        client.close()
    }
}