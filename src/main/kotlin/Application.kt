package com.example

import com.example.Routes.productRoutes
import com.example.Service.ProductService
import com.example.db.DatabaseConfig
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

fun main(args: Array<String>) {
    EngineMain.main(args)
}
fun Application.module() {
    install(ContentNegotiation) {
        json()
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

    val productService = ProductService(DatabaseConfig.productCollection)

    routing {
        productRoutes(productService)
    }
}
