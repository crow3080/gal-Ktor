package com.example.utils

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*


suspend fun ApplicationCall.ensureAdminSession(): Boolean {
    val session = sessions.get<AdminSession>()
    return if (session == null) {
        respondRedirect("/login")
        false
    } else true
}