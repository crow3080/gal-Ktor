package com.example

import com.example.Routes.categoryRoutes
import com.example.Routes.productRoutes
import com.example.Service.CategoryService
import com.example.Service.FileUploadService
import com.example.Service.ProductService
import com.example.db.DatabaseConfig
import com.example.di.appModules
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.io.File

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    install(Koin) {
        slf4jLogger()
        modules(appModules)
    }

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
    val categoryService = CategoryService(DatabaseConfig.categoryCollection)
    val fileUploadService = FileUploadService()  // ✅

    routing {
        staticResources("/", "static")
        staticFiles("/uploads", File("uploads"))
        productRoutes(productService, fileUploadService)  // ✅
        categoryRoutes(categoryService, fileUploadService)  // ✅

    }

}