package com.example.Routes

import com.example.Service.FileUploadService
import com.example.Service.ProductService
import com.example.db.models.Product
import com.example.utils.ApiResponse
import com.example.utils.ensureAdminSession
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*

fun Route.productRoutes(
    productService: ProductService,
    fileUploadService: FileUploadService
) {


    get("/products") {
        if (!call.ensureAdminSession()) return@get
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

        post("/with-image") {
            try {
                val multipart = call.receiveMultipart()
                var name = ""
                var price = 0.0
                var description = ""
                var categoryId = ""
                var imageUrl: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "price" -> price = part.value.toDoubleOrNull() ?: 0.0
                                "description" -> description = part.value
                                "categoryId" -> categoryId = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            val fileBytes = part.streamProvider().readBytes()
                            val originalFileName = part.originalFileName ?: "image.jpg"
                            val fileExtension = originalFileName.substringAfterLast(".")
                            val fileName = "${java.util.UUID.randomUUID()}.$fileExtension"
                            val file = java.io.File("uploads/images/$fileName")
                            file.parentFile.mkdirs()
                            file.writeBytes(fileBytes)
                            imageUrl = "/uploads/images/$fileName"
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                val product = Product(
                    name = name,
                    price = price,
                    description = description,
                    categoryId = categoryId,
                    imageUrl = imageUrl
                )

                val validationError = productService.validateProduct(product)
                if (validationError != null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, validationError)
                    )
                    return@post
                }

                val categoryExists = productService.categoryExists(product.categoryId)
                if (!categoryExists) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "التصنيف المحدد غير موجود")
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

                val categoryExists = productService.categoryExists(product.categoryId)
                if (!categoryExists) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "التصنيف المحدد غير موجود")
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

                val product = productService.getProductById(id)
                product?.imageUrl?.let { fileUploadService.deleteImage(it) }

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