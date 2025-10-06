package com.example

import com.example.data.Di.appModule
import com.example.data.driver.initDatabase
import com.example.routes.admin.adminRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun main(args: Array<String>) {
    EngineMain.main(args)
}
fun Application.module() {
    initDatabase()

    install(ContentNegotiation) { json() }

    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    routing {
   adminRoutes()
    }
}