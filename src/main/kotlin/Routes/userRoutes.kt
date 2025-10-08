package com.example.Routes

import com.example.Services.UserService
import com.example.data.models.DTO.UserDTO
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/users") {
        get {
            call.respond(userService.getAllUsers())
        }

        post {
            val user = call.receive<UserDTO>()
            val id = userService.addUser(user)
            call.respondText("User added with id: $id")
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respondText("Invalid id")
            val user = call.receive<UserDTO>()
            val updated = userService.updateUser(id, user)
            if (updated)
                call.respondText("User $id updated successfully!")
            else
                call.respondText("User not found!")
        }
    }
}
