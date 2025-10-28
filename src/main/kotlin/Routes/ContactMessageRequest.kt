package com.example.Routes

import com.example.Service.ContactMessageService
import com.example.db.models.ContactMessage
import com.example.utils.session.ensureAdminSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import kotlinx.serialization.Serializable

@Serializable
data class ContactMessageRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val message: String
)

@Serializable
data class ContactMessageResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val message: String,
    val isRead: Boolean,
    val createdAt: Long
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class MessagesResponse(
    val data: List<ContactMessageResponse>,
    val total: Long,
    val unreadCount: Long,
    val page: Int,
    val totalPages: Int
)

fun ContactMessage.toResponse() = ContactMessageResponse(
    id = _id.toString(),
    name = name,
    email = email,
    phone = phone,
    message = message,
    isRead = isRead,
    createdAt = createdAt
)

fun Route.contactRoutes(service: ContactMessageService) {

    // صفحة إدارة الرسائل
    get("/messages") {
        if (!call.ensureAdminSession()) return@get
        call.respond(ThymeleafContent("messages", mapOf()))
    }

    // API للعملاء - إرسال رسالة
    route("/api/contact") {
        post {
            try {
                println("📧 [START] Received contact form submission")
                println("📧 [HEADERS] Content-Type: ${call.request.contentType()}")
                println("📧 [HEADERS] Accept: ${call.request.headers["Accept"]}")

                // قراءة البيانات
                val request = try {
                    call.receive<ContactMessageRequest>()
                } catch (e: Exception) {
                    println("❌ [ERROR] Failed to parse request body: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<Nothing>(
                            success = false,
                            error = "صيغة البيانات غير صحيحة"
                        )
                    )
                    return@post
                }

                println("📝 [DATA] name=${request.name}, email=${request.email}, message=${request.message.take(50)}...")

                // التحقق من البيانات
                when {
                    request.name.isBlank() -> {
                        println("❌ [VALIDATION] Name is blank")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Nothing>(success = false, error = "الاسم مطلوب")
                        )
                        return@post
                    }
                    request.email.isBlank() -> {
                        println("❌ [VALIDATION] Email is blank")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Nothing>(success = false, error = "البريد الإلكتروني مطلوب")
                        )
                        return@post
                    }
                    !request.email.contains("@") -> {
                        println("❌ [VALIDATION] Email format invalid")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Nothing>(success = false, error = "صيغة البريد الإلكتروني غير صحيحة")
                        )
                        return@post
                    }
                    request.message.isBlank() -> {
                        println("❌ [VALIDATION] Message is blank")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse<Nothing>(success = false, error = "الرسالة مطلوبة")
                        )
                        return@post
                    }
                }

                // إنشاء الرسالة
                val contactMessage = ContactMessage(
                    name = request.name.trim(),
                    email = request.email.trim().lowercase(),
                    phone = request.phone?.trim(),
                    message = request.message.trim()
                )

                println("💾 [SAVE] Saving message to database...")

                val created = try {
                    service.createMessage(contactMessage)
                } catch (e: Exception) {
                    println("❌ [DB ERROR] Failed to save: ${e.message}")
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<Nothing>(
                            success = false,
                            error = "فشل حفظ الرسالة في قاعدة البيانات"
                        )
                    )
                    return@post
                }

                println("✅ [SUCCESS] Message saved with ID: ${created._id}")

                // إرجاع الاستجابة
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(
                        success = true,
                        message = "تم إرسال رسالتك بنجاح. سنتواصل معك قريباً!",
                        data = created.toResponse()
                    )
                )

            } catch (e: ContentTransformationException) {
                println("❌ [PARSE ERROR] ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "صيغة البيانات غير صحيحة"
                    )
                )
            } catch (e: Exception) {
                println("❌ [UNEXPECTED ERROR] ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "حدث خطأ غير متوقع. يرجى المحاولة مرة أخرى."
                    )
                )
            }
        }
    }

    // API للإدارة - إدارة الرسائل
    route("/api/admin/messages") {

        // الحصول على جميع الرسائل
        get {
            if (!call.ensureAdminSession()) return@get

            try {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val readFilter = call.request.queryParameters["read"]?.toBooleanStrictOrNull()
                val search = call.request.queryParameters["search"]

                val messages = if (!search.isNullOrBlank()) {
                    service.searchMessages(search)
                } else {
                    service.getAllMessages(page, limit, readFilter)
                }

                val total = service.getMessagesCount(readFilter)
                val unreadCount = service.getMessagesCount(false)

                val messagesResponse = messages.map { it.toResponse() }

                call.respond(
                    MessagesResponse(
                        data = messagesResponse,
                        total = total,
                        unreadCount = unreadCount,
                        page = page,
                        totalPages = ((total + limit - 1) / limit).toInt()
                    )
                )
            } catch (e: Exception) {
                println("❌ Error loading messages: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "فشل في جلب الرسائل"
                    )
                )
            }
        }

        // تحديث حالة القراءة
        patch("/{id}/read") {
            if (!call.ensureAdminSession()) return@patch

            try {
                val id = call.parameters["id"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "معرف الرسالة مطلوب")
                    )
                    return@patch
                }

                val isRead = call.request.queryParameters["isRead"]?.toBooleanStrictOrNull() ?: true

                val updated = service.markAsRead(id, isRead)
                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("success" to true, "message" to "تم التحديث بنجاح")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "الرسالة غير موجودة")
                    )
                }
            } catch (e: Exception) {
                println("❌ Error updating message: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "فشل في تحديث الرسالة")
                )
            }
        }

        // حذف رسالة
        delete("/{id}") {
            if (!call.ensureAdminSession()) return@delete

            try {
                val id = call.parameters["id"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "معرف الرسالة مطلوب")
                    )
                    return@delete
                }

                val deleted = service.deleteMessage(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "الرسالة غير موجودة")
                    )
                }
            } catch (e: Exception) {
                println("❌ Error deleting message: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "فشل في حذف الرسالة")
                )
            }
        }
    }
}