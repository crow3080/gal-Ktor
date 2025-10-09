package com.example.di

import com.example.Service.ProductService
import com.example.db.DatabaseConfig
import org.koin.dsl.module

val databaseModule = module {
    single { DatabaseConfig.productCollection }
}


val serviceModule = module {
    single { ProductService(get()) }
}


val appModules = listOf(
    databaseModule,
    serviceModule
)