package com.example.Service

import java.io.File

class FileUploadService {

    private val uploadDir = "uploads/images"

    init {
        File(uploadDir).mkdirs()
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