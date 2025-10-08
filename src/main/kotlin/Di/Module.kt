package com.example.Di

import com.example.Di.Repositorys.UserRepository
import com.example.Services.UserService
import org.koin.dsl.module

val appModule = module {
    single { UserRepository() }
    single { UserService(get()) }
}
