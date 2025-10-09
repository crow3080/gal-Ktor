package com.example.Service

import io.ktor.http.content.*
import java.io.File
import java.util.*

class FileUploadService {

    private val uploadDir = "uploads/images"

    init {
        File(uploadDir).mkdirs()
    }

    suspend fun saveImage(multipart: MultiPartData): String? {
        var fileName: String? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val fileBytes = part.streamProvider().readBytes()
                    val originalFileName = part.originalFileName ?: "image.jpg"
                    val fileExtension = originalFileName.substringAfterLast(".")

                    // إنشاء اسم فريد للملف
                    fileName = "${UUID.randomUUID()}.$fileExtension"
                    val file = File("$uploadDir/$fileName")

                    file.writeBytes(fileBytes)
                }
                else -> {}
            }
            part.dispose()
        }

        return fileName?.let { "/uploads/images/$it" }
    }

    fun deleteImage(imageUrl: String) {
        try {
            val fileName = imageUrl.substringAfterLast("/")
            val file = File("$uploadDir/$fileName")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            println("خطأ في حذف الصورة: ${e.message}")
        }
    }
}