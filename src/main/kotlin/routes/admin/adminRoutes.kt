package com.example.routes.admin

import com.example.data.model.DTO.CategoryDTO
import com.example.data.model.DTO.ProductDTO
import com.example.services.admin.CategoryService
import com.example.services.admin.ProductService
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File
import io.ktor.server.request.*

fun Route.adminRoutes() {

    get("/") {
        call.respondFile(File("src/main/resources/templates/index.html"))
    }

    get("/categories") {
        val categoryService = getKoin().get<CategoryService>()
        val categories = categoryService.getAllCategories()
        call.respond(categories)
    }

    post("/categories") {
        val categoryService = getKoin().get<CategoryService>()
        val body = call.receive<CategoryDTO>()
        val created = categoryService.createCategory(body)
        call.respond(created)
    }

    put("/categories/{id}") {
        val categoryService = getKoin().get<CategoryService>()
        val id = call.parameters["id"] ?: return@put call.respondText("Missing ID")
        val body = call.receive<CategoryDTO>()
        val updated = categoryService.updateCategory(id, body)
        call.respond(mapOf("updated" to updated))
    }

    delete("/categories/{id}") {
        val categoryService = getKoin().get<CategoryService>()
        val id = call.parameters["id"] ?: return@delete call.respondText("Missing ID")
        val deleted = categoryService.deleteCategory(id)
        call.respond(mapOf("deleted" to deleted))
    }
    get("/products") {
        val productService = getKoin().get<ProductService>()
        val products = productService.getAllProducts()
        call.respond(products)
    }

    post("/products") {
        val productService = getKoin().get<ProductService>()
        val body = call.receive<ProductDTO>()
        val created = productService.createProduct(body)
        call.respond(created)
    }

    put("/products/{id}") {
        val productService = getKoin().get<ProductService>()
        val id = call.parameters["id"] ?: return@put call.respondText("Missing ID")
        val body = call.receive<ProductDTO>()
        val updated = productService.updateProduct(id, body)
        call.respond(mapOf("updated" to updated))
    }

    delete("/products/{id}") {
        val productService = getKoin().get<ProductService>()
        val id = call.parameters["id"] ?: return@delete call.respondText("Missing ID")
        val deleted = productService.deleteProduct(id)
        call.respond(mapOf("deleted" to deleted))
    }
}