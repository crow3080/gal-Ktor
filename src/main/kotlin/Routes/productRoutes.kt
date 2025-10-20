package com.example.Routes

import com.example.Service.FileUploadService
import com.example.Service.ProductService
import com.example.db.models.Product
import com.example.utils.Responses.ApiResponse
import com.example.utils.Responses.ImportResponse
import com.example.utils.session.ensureAdminSession
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import java.io.File

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

        // ✅ نقل endpoint الاستيراد هنا داخل route /api/products
        post("/import") {
            try {
                val multipart = call.receiveMultipart()
                var csvFile: File? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val fileBytes = part.streamProvider().readBytes()
                        val fileName = part.originalFileName ?: "products.csv"
                        csvFile = File("uploads/temp/$fileName")
                        csvFile!!.parentFile.mkdirs()
                        csvFile!!.writeBytes(fileBytes)
                    }
                    part.dispose()
                }

                if (csvFile == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "ملف CSV مفقود")
                    )
                    return@post
                }

                val addedProducts = mutableListOf<Product>()
                val errors = mutableListOf<String>()

                val content = csvFile!!.readText(Charsets.UTF_8)
                println("📄 محتوى الملف:")
                println(content)

                val lines = content.lines().filter { it.isNotBlank() }
                println("📊 عدد الأسطر: ${lines.size}")

                // تجاهل الصف الأول (العناوين)
                for (i in 1 until lines.size) {
                    try {
                        val line = lines[i].trim()
                        println("\n🔍 معالجة السطر $i: $line")

                        val parts = line.split(",").map { it.trim() }
                        println("   الأجزاء (${parts.size}): ${parts.joinToString(" | ")}")

                        if (parts.size < 4) {
                            val error = "السطر $i: عدد أعمدة غير كافي (${parts.size}/4)"
                            errors.add(error)
                            println("   ❌ $error")
                            continue
                        }

                        val name = parts[0]
                        val priceStr = parts[1]
                        val description = parts[2]
                        val categoryId = parts[3]
                        val imageUrl = if (parts.size > 4 && parts[4].isNotBlank()) parts[4] else null

                        println("   📦 اسم: $name")
                        println("   💰 سعر: $priceStr")
                        println("   📝 وصف: $description")
                        println("   🏷️  تصنيف: $categoryId")
                        println("   🖼️  صورة: $imageUrl")

                        val price = priceStr.toDoubleOrNull()
                        if (price == null || price <= 0) {
                            val error = "السطر $i: السعر غير صحيح ($priceStr)"
                            errors.add(error)
                            println("   ❌ $error")
                            continue
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
                            val error = "السطر $i: $validationError"
                            errors.add(error)
                            println("   ❌ $error")
                            continue
                        }

                        val categoryExists = productService.categoryExists(categoryId)
                        println("   🔎 التصنيف موجود؟ $categoryExists")

                        if (!categoryExists) {
                            val error = "السطر $i: التصنيف '$categoryId' غير موجود"
                            errors.add(error)
                            println("   ❌ $error")
                            continue
                        }

                        val newProduct = productService.createProduct(product)
                        addedProducts.add(newProduct)
                        println("   ✅ تم إضافة المنتج بنجاح!")

                    } catch (e: Exception) {
                        val error = "السطر $i: ${e.message}"
                        errors.add(error)
                        println("   ❌ خطأ: ${e.message}")
                        e.printStackTrace()
                    }
                }

                // حذف الملف المؤقت
                csvFile!!.delete()

                println("\n📊 النتيجة النهائية:")
                println("   ✅ نجح: ${addedProducts.size}")
                println("   ❌ فشل: ${errors.size}")

                val message = if (errors.isNotEmpty()) {
                    "تم استيراد ${addedProducts.size} منتج. فشل ${errors.size}: ${errors.take(3).joinToString("; ")}"
                } else {
                    "تم استيراد ${addedProducts.size} منتج بنجاح"
                }

                call.respond(
                    HttpStatusCode.Created,
                    ImportResponse(
                        success = true,
                        message = message,
                        data = addedProducts
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(false, "خطأ في استيراد المنتجات: ${e.message}")
                )
            }
        }
    }
}