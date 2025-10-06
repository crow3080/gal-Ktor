package com.example.services.admin


import com.example.data.model.DTO.ProductDTO
import com.example.data.model.Products
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class ProductService(private val categoryService: CategoryService) {
    fun getAllProducts(): List<ProductDTO> = transaction {
        Products.selectAll().map {
            ProductDTO(
                it[Products.id].value.toString(),
                it[Products.name],
                it[Products.price],
                it[Products.categoryId].value.toString()
            )
        }
    }

    fun createProduct(dto: ProductDTO): ProductDTO {
        val id = UUID.randomUUID()
        transaction {
            Products.insert {
                it[this.id] = id
                it[name] = dto.name
                it[price] = dto.price
                it[categoryId] = UUID.fromString(dto.categoryId)
            }
        }
        return ProductDTO(id.toString(), dto.name, dto.price, dto.categoryId)
    }

    fun updateProduct(id: String, dto: ProductDTO): Int = transaction {
        Products.update({ Products.id eq UUID.fromString(id) }) {
            it[name] = dto.name
            it[price] = dto.price
            it[categoryId] = UUID.fromString(dto.categoryId)
        }
    }

    fun deleteProduct(id: String): Int = transaction {
        Products.deleteWhere { Products.id eq UUID.fromString(id) }
    }
}