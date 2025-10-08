package com.example

import com.example.Di.appModule
import com.example.Routes.userRoutes
import com.example.Services.UserService
import com.example.data.factory.DatabaseFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

fun main(args: Array<String>) {
    EngineMain.main(args)
}


fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }

    install(Koin) {
        modules(appModule)
    }

    install(Thymeleaf) {
        setTemplateResolver(
            ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                characterEncoding = "utf-8"
            }
        )
    }

    DatabaseFactory.init()

    val userService by inject<UserService>()

    routing {
        get("/") {
            val users = userService.getAllUsers()
            call.respond(ThymeleafContent("index", mapOf("users" to users)))
        }

        userRoutes(userService)
    }
}