package com.example.Routes

import com.example.Service.CategoryService
import com.example.Service.FileUploadService
import com.example.db.models.Category
import com.example.utils.Responses.CategoryApiResponse
import com.example.utils.session.ensureAdminSession
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import java.io.File
import java.util.UUID

fun Route.categoryRoutes(
    categoryService: CategoryService,
    fileUploadService: FileUploadService
) {

    get("/categories") {
        if (!call.ensureAdminSession()) return@get
        call.respond(ThymeleafContent("categories", mapOf()))
    }


    route("/api/categories") {

        get {
            try {
                val categories = categoryService.getAllCategories()
                call.respond(HttpStatusCode.OK, categories)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    CategoryApiResponse(false, "خطأ في جلب التصنيفات: ${e.message}")
                )
            }
        }

        post("/with-image") {
            try {
                val multipart = call.receiveMultipart()
                var nameAr = ""
                var nameEn = ""
                var imageUrl: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "nameAr" -> nameAr = part.value
                                "nameEn" -> nameEn = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            val fileBytes = part.streamProvider().readBytes()
                            val originalFileName = part.originalFileName ?: "image.jpg"
                            val fileExtension = originalFileName.substringAfterLast(".")
                            val fileName = "${UUID.randomUUID()}.$fileExtension"
                            val file = File("uploads/images/$fileName")
                            file.parentFile.mkdirs()
                            file.writeBytes(fileBytes)
                            imageUrl = "/uploads/images/$fileName"
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (nameAr.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CategoryApiResponse(false, "اسم التصنيف بالعربية مطلوب")
                    )
                    return@post
                }

                val category = Category(nameAr = nameAr, nameEn = nameEn, imageUrl = imageUrl)
                val newCategory = categoryService.createCategory(category)

                call.respond(
                    HttpStatusCode.Created,
                    CategoryApiResponse(true, "تمت الإضافة بنجاح", newCategory)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    CategoryApiResponse(false, "خطأ: ${e.message}")
                )
            }
        }

        post {
            try {
                val category = call.receive<Category>()

                if (category.nameAr.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CategoryApiResponse(false, "اسم التصنيف بالعربية مطلوب")
                    )
                    return@post
                }

                val newCategory = categoryService.createCategory(category)
                call.respond(
                    HttpStatusCode.Created,
                    CategoryApiResponse(true, "تمت الإضافة بنجاح", newCategory)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    CategoryApiResponse(false, "خطأ في إضافة التصنيف: ${e.message}")
                )
            }
        }

        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("المعرف مفقود")
                val category = call.receive<Category>()

                if (category.nameAr.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CategoryApiResponse(false, "اسم التصنيف بالعربية مطلوب")
                    )
                    return@put
                }

                val updated = categoryService.updateCategory(id, category)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        CategoryApiResponse(true, "تم التحديث بنجاح")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        CategoryApiResponse(false, "التصنيف غير موجود")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    CategoryApiResponse(false, "خطأ في التحديث: ${e.message}")
                )
            }
        }

        put("/{id}/with-image") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("المعرف مفقود")
                val multipart = call.receiveMultipart()
                var nameAr = ""
                var nameEn = ""
                var imageUrl: String? = null
                var removeImage = false

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "nameAr" -> nameAr = part.value
                                "nameEn" -> nameEn = part.value
                                "removeImage" -> removeImage = part.value == "true"
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "image") {
                                val fileBytes = part.streamProvider().readBytes()
                                val originalFileName = part.originalFileName ?: "image.jpg"
                                val fileExtension = originalFileName.substringAfterLast(".")
                                val fileName = "${UUID.randomUUID()}.$fileExtension"
                                val file = File("uploads/images/$fileName")
                                file.parentFile.mkdirs()
                                file.writeBytes(fileBytes)
                                imageUrl = "/uploads/images/$fileName"
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (nameAr.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CategoryApiResponse(false, "اسم التصنيف بالعربية مطلوب")
                    )
                    return@put
                }

                // جلب التصنيف الحالي للحصول على imageUrl القديم إذا لزم الأمر
                val existingCategory = categoryService.getCategoryById(id)
                    ?: return@put call.respond(
                        HttpStatusCode.NotFound,
                        CategoryApiResponse(false, "التصنيف غير موجود")
                    )

                // التعامل مع إزالة الصورة
                if (removeImage) {
                    existingCategory.imageUrl?.let { fileUploadService.deleteImage(it) }
                    imageUrl = null
                } else if (imageUrl != null) {
                    // إذا تم تحميل صورة جديدة، حذف القديمة
                    existingCategory.imageUrl?.let { fileUploadService.deleteImage(it) }
                } else {
                    // إذا لم يتم تغيير الصورة، الاحتفاظ بالقديمة
                    imageUrl = existingCategory.imageUrl
                }

                val updatedCategory = Category(
                    _id = id,
                    nameAr = nameAr,
                    nameEn = nameEn,
                    imageUrl = imageUrl
                )

                val updated = categoryService.updateCategory(id, updatedCategory)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        CategoryApiResponse(true, "تم التحديث بنجاح", updatedCategory)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        CategoryApiResponse(false, "التصنيف غير موجود")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    CategoryApiResponse(false, "خطأ في التحديث: ${e.message}")
                )
            }
        }

        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("المعرف مفقود")

                val category = categoryService.getCategoryById(id)
                category?.imageUrl?.let { fileUploadService.deleteImage(it) }

                val deleted = categoryService.deleteCategory(id)

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        CategoryApiResponse(true, "تم الحذف بنجاح")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        CategoryApiResponse(false, "التصنيف غير موجود")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    CategoryApiResponse(false, "خطأ في الحذف: ${e.message}")
                )
            }
        }
    }
    post("/api/categories/import") {
        try {
            val multipart = call.receiveMultipart()
            var csvFile: File? = null

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val fileBytes = part.streamProvider().readBytes()
                    val fileName = part.originalFileName ?: "categories.csv"
                    csvFile = File("uploads/temp/$fileName")
                    csvFile!!.parentFile.mkdirs()
                    csvFile!!.writeBytes(fileBytes)
                }
                part.dispose()
            }

            if (csvFile == null) {
                call.respond(HttpStatusCode.BadRequest, "ملف CSV مفقود")
                return@post
            }
            val categories = csvFile!!.readLines()
                .drop(1)
                .mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size >= 3)
                        Category(nameAr = parts[0], nameEn = parts[1], imageUrl = parts[2].takeIf { it.isNotBlank() })
                    else null
                }

            categories.forEach {
                categoryService.createCategory(it)
            }

            call.respond(HttpStatusCode.OK, "تم استيراد ${categories.size} تصنيف بنجاح ✅")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "خطأ أثناء الاستيراد: ${e.message}")
        }
    }

}