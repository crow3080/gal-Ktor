package com.example.di

import com.example.Service.CategoryService
import com.example.Service.ContactMessageService
import com.example.Service.FileUploadService
import com.example.Service.ProductService
import com.example.db.DatabaseConfig
import org.koin.dsl.module

val databaseModule = module {
    single { DatabaseConfig.productCollection }
    single { DatabaseConfig.categoryCollection }
    single { DatabaseConfig.contactMessageCollection }
}

val serviceModule = module {
    single { ProductService(get()) }
    single { CategoryService(DatabaseConfig.categoryCollection) }
    single { ContactMessageService(DatabaseConfig.contactMessageCollection) }
    single { FileUploadService() }
}

val appModules = listOf(
    databaseModule,
    serviceModule
)