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
                val page = call.parameters["page"]?.toIntOrNull() ?: 1
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 12
                val search = call.parameters["search"]?.trim() ?: ""
                val category = call.parameters["category"]?.trim() ?: ""
                val sort = call.parameters["sort"]?.trim() ?: ""

                val result = productService.getProductsPaginated(
                    page = page,
                    limit = limit,
                    search = search,
                    category = if (category.isNotEmpty()) category else null,
                    sort = if (sort.isNotEmpty()) sort else null
                )
                call.respond(HttpStatusCode.OK, result)
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

                val categoryExists = productService.categoryExists(product.categoryId)
                if (!categoryExists) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "التصنيف المحدد غير موجود")
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

        put("/{id}/with-image") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("المعرف مفقود")
                val multipart = call.receiveMultipart()
                var name = ""
                var price = 0.0
                var description = ""
                var categoryId = ""
                var imageUrl: String? = null
                var removeImage = false

                // جلب المنتج الحالي للحصول على الصورة القديمة
                val currentProduct = productService.getProductById(id)
                if (currentProduct == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(false, "المنتج غير موجود")
                    )
                    return@put
                }

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "price" -> price = part.value.toDoubleOrNull() ?: 0.0
                                "description" -> description = part.value
                                "categoryId" -> categoryId = part.value
                                "removeImage" -> removeImage = part.value == "true"
                            }
                        }
                        is PartData.FileItem -> {
                            val fileBytes = part.streamProvider().readBytes()
                            val originalFileName = part.originalFileName ?: "image.jpg"
                            val fileExtension = originalFileName.substringAfterLast(".")
                            val fileName = "${java.util.UUID.randomUUID()}.$fileExtension"
                            val file = File("uploads/images/$fileName")
                            file.parentFile.mkdirs()
                            file.writeBytes(fileBytes)
                            imageUrl = "/uploads/images/$fileName"
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                // إذا تم طلب إزالة الصورة، تعيين imageUrl إلى null
                if (removeImage) {
                    imageUrl = null
                    currentProduct.imageUrl?.let { fileUploadService.deleteImage(it) }
                } else if (imageUrl != null) {
                    // إذا تم رفع صورة جديدة، حذف الصورة القديمة إن وجدت
                    currentProduct.imageUrl?.let { fileUploadService.deleteImage(it) }
                } else {
                    // الاحتفاظ بالصورة الحالية إذا لم يتم رفع صورة جديدة ولم يتم طلب الإزالة
                    imageUrl = currentProduct.imageUrl
                }

                val product = Product(
                    _id = id,
                    name = name,
                    price = price,
                    description = description,
                    categoryId = categoryId,
                    imageUrl = imageUrl
                )

                val validationError = productService.validateProduct(product)
                if (validationError != null) {
                    // حذف الصورة المرفوعة إذا فشل التحقق
                    imageUrl?.let { if (!removeImage && it != currentProduct.imageUrl) fileUploadService.deleteImage(it) }
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, validationError)
                    )
                    return@put
                }

                val categoryExists = productService.categoryExists(product.categoryId)
                if (!categoryExists) {
                    // حذف الصورة المرفوعة إذا فشل التحقق
                    imageUrl?.let { if (!removeImage && it != currentProduct.imageUrl) fileUploadService.deleteImage(it) }
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(false, "التصنيف المحدد غير موجود")
                    )
                    return@put
                }

                val updated = productService.updateProduct(id, product)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(true, "تم تحديث المنتج بنجاح", product)
                    )
                } else {
                    // حذف الصورة المرفوعة إذا فشل التحديث
                    imageUrl?.let { if (!removeImage && it != currentProduct.imageUrl) fileUploadService.deleteImage(it) }
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(false, "المنتج غير موجود")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(false, "خطأ في تحديث المنتج: ${e.message}")
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
                val lines = content.lines().filter { it.isNotBlank() }

                for (i in 1 until lines.size) {
                    try {
                        val line = lines[i].trim()
                        val parts = line.split(",").map { it.trim() }

                        if (parts.isEmpty() || parts[0].isBlank()) {
                            errors.add("السطر $i: اسم المنتج فارغ")
                            continue
                        }

                        val name = parts.getOrNull(0) ?: ""
                        val priceStr = parts.getOrNull(1) ?: ""
                        var description = parts.getOrNull(2) ?: ""
                        val categoryId = parts.getOrNull(3) ?: ""
                        val imageUrl = parts.getOrNull(4)?.takeIf { it.isNotBlank() }

                        val price = priceStr.toDoubleOrNull()
                        if (price == null || price <= 0) {
                            errors.add("السطر $i: السعر غير صحيح ($priceStr)")
                            continue
                        }

                        if (description.isBlank()) {
                            description = name
                        }

                        val product = Product(
                            name = name,
                            price = price,
                            description = description,
                            categoryId = categoryId,
                            imageUrl = imageUrl
                        )

                        if (categoryId.isNotBlank()) {
                            val categoryExists = productService.categoryExists(categoryId)
                            if (!categoryExists) {
                                errors.add("السطر $i: التصنيف '$categoryId' غير موجود")
                                continue
                            }
                        }

                        val newProduct = productService.createProduct(product)
                        addedProducts.add(newProduct)

                    } catch (e: Exception) {
                        errors.add("السطر $i: ${e.message}")
                    }
                }

                csvFile!!.delete()

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