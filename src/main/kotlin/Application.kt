package com.example

import com.example.Routes.categoryRoutes
import com.example.Routes.contactRoutes
import com.example.Routes.productRoutes
import com.example.Service.CategoryService
import com.example.Service.ContactMessageService
import com.example.Service.FileUploadService
import com.example.Service.ProductService
import com.example.db.DatabaseConfig
import com.example.di.appModules
import com.example.utils.session.AdminSession
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.*
import kotlinx.serialization.json.Json
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

    // إضافة CORS لحل مشاكل الاتصال
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowCredentials = true
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    install(Sessions) {
        cookie<AdminSession>("ADMIN_SESSION") {
            cookie.path = "/"
            cookie.httpOnly = true
        }
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
    val contactMessageService = ContactMessageService(DatabaseConfig.contactMessageCollection)
    val fileUploadService = FileUploadService()

    routing {
        staticResources("/", "static")
        staticFiles("/uploads", File("uploads"))

        productRoutes(productService, fileUploadService)
        categoryRoutes(categoryService, fileUploadService)
        contactRoutes(contactMessageService)

        get("/login") {
            call.respond(ThymeleafContent("login", mapOf()))
        }

        post("/login") {
            val params = call.receiveParameters()
            val username = params["username"]
            val password = params["password"]

            if (username == "admin" && password == "1234") {
                call.sessions.set(AdminSession(username))
                call.respondRedirect("/products")
            } else {
                call.respond(ThymeleafContent("login", mapOf("error" to "بيانات الدخول غير صحيحة")))
            }
        }

        get("/logout") {
            call.sessions.clear<AdminSession>()
            call.respondRedirect("/login")
        }

        get("/") {
            call.respond(ThymeleafContent("main", mapOf()))
        }

        get("/contact") {
            call.respond(ThymeleafContent("contact", mapOf()))
        }

        get("/cart") {
            call.respond(ThymeleafContent("cart", mapOf()))
        }

        get("/priceReq") {
            call.respond(ThymeleafContent("priceReq", mapOf()))
        }

        get("/productCatalog") {
            call.respond(ThymeleafContent("productCatalog", mapOf()))
        }

        get("/clientLogin") {
            call.respond(ThymeleafContent("clientlogin", mapOf()))
        }

        get("/register") {
            call.respond(ThymeleafContent("register", mapOf()))
        }
    }
}