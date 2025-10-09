package com.example.Routes

import com.example.Service.ProductService
import com.example.db.models.Product
import com.example.utils.ApiResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*

fun Route.productRoutes(productService: ProductService) {

    get("/products") {
        call.respond(ThymeleafContent("index", mapOf()))
    }

    route("/api/products") {

        get {
            try {
                val products = productService.getAllProducts()
                call.respond(HttpStatusCode.OK, products)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "خطأ في جلب المنتجات: ${e.message}")
                )
            }
        }

        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("المعرف مفقود")
                val product = productService.getProductById(id)

                if (product != null) {
                    call.respond(HttpStatusCode.OK, product)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(false, "المنتج غير موجود")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "خطأ: ${e.message}")
                )
            }
        }

        post {
            try {
                val product = call.receive<Product>()

                val validationError = productService.validateProduct(product)
                if (validationError != null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, validationError)
                    )
                    return@post
                }

                val newProduct = productService.createProduct(product)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(true, "تمت إضافة المنتج بنجاح", newProduct)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, "خطأ في إضافة المنتج: ${e.message}")
                )
            }
        }

        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("المعرف مفقود")
                val product = call.receive<Product>()

                val validationError = productService.validateProduct(product)
                if (validationError != null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, validationError)
                    )
                    return@put
                }

                val updated = productService.updateProduct(id, product)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(true, "تم تحديث المنتج بنجاح")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(false, "المنتج غير موجود")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, "خطأ في التحديث: ${e.message}")
                )
            }
        }

        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("المعرف مفقود")
                val deleted = productService.deleteProduct(id)

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(true, "تم حذف المنتج بنجاح")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(false, "المنتج غير موجود")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "خطأ في الحذف: ${e.message}")
                )
            }
        }
    }
}