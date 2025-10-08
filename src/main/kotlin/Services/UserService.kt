package com.example.Services

import com.example.Di.Repositorys.UserRepository
import com.example.data.models.DTO.UserDTO

class UserService(private val repo: UserRepository) {
    fun getAllUsers() = repo.getAllUsers()
    fun addUser(user: UserDTO) = repo.addUser(user)
    fun updateUser(id: Int, user: UserDTO) = repo.updateUser(id, user)
}

