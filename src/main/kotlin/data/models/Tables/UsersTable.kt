package com.example.data.models.Tables

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val email = varchar("email", 100)
    val age = integer("age")
    val photo = text("photo").nullable()
    override val primaryKey = PrimaryKey(id)
}

object CategoryTable : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val photo = text("photo").nullable()
    override val primaryKey = PrimaryKey(id)
}