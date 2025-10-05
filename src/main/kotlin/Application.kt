package com.example

import com.example.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
}


fun Route.app() {
    get("/person") {
        call.respondText("Hello World!")
    }
}


