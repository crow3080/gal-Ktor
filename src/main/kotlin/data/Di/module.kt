package com.example.data.Di


import com.example.services.admin.CategoryService
import com.example.services.admin.ProductService
import org.koin.dsl.module

val appModule = module {
    single { CategoryService() }
    single { ProductService(get()) }
}