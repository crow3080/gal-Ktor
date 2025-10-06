package com.example.services.admin


import com.example.data.model.Categories
import com.example.data.model.DTO.CategoryDTO
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class CategoryService {
    fun getAllCategories(): List<CategoryDTO> = transaction {
        Categories.selectAll().map {
            CategoryDTO(it[Categories.id].value.toString(), it[Categories.name])
        }
    }

    fun createCategory(dto: CategoryDTO): CategoryDTO {
        val id = UUID.randomUUID()
        transaction {
            Categories.insert {
                it[this.id] = id
                it[name] = dto.name
            }
        }
        return CategoryDTO(id.toString(), dto.name)
    }

    fun updateCategory(id: String, dto: CategoryDTO): Int = transaction {
        Categories.update({ Categories.id eq UUID.fromString(id) }) {
            it[name] = dto.name
        }
    }

    fun deleteCategory(id: String): Int = transaction {
        Categories.deleteWhere { Categories.id eq UUID.fromString(id) }
    }
}


