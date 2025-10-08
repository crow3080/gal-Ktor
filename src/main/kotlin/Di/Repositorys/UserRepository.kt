package com.example.Di.Repositorys

import com.example.data.models.DTO.UserDTO
import com.example.data.models.Tables.UsersTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class UserRepository {
    fun getAllUsers(): List<Map<String, Any?>> = transaction {
        UsersTable.selectAll().map {
            mapOf(
                "id" to it[UsersTable.id],
                "name" to it[UsersTable.name],
                "email" to it[UsersTable.email],
                "age" to it[UsersTable.age],
                "photo" to it[UsersTable.photo]
            )
        }
    }

    fun addUser(user: UserDTO): Int = transaction {
        UsersTable.insert {
            it[name] = user.name
            it[email] = user.email
            it[age] = user.age
            it[photo] = user.photo
        } get UsersTable.id
    }

    fun updateUser(id: Int, user: UserDTO): Boolean = transaction {
        UsersTable.update({ UsersTable.id eq id }) {
            it[name] = user.name
            it[email] = user.email
            it[age] = user.age
            it[photo] = user.photo
        } > 0
    }
}