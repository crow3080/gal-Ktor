package com.example.plugins

import com.example.app
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        app()
    }
}
