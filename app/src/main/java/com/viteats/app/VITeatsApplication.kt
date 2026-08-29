package com.viteats.app

import android.app.Application
import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.NetworkModule
import com.viteats.app.data.repository.AuthRepository
import com.viteats.app.data.repository.MenuRepository
import com.viteats.app.data.repository.OrderRepository
import com.viteats.app.data.repository.StudentRepository

class VITeatsApplication : Application() {
    lateinit var sessionManager: SessionManager
    lateinit var authRepository: AuthRepository
    lateinit var studentRepository: StudentRepository
    lateinit var menuRepository: MenuRepository
    lateinit var orderRepository: OrderRepository

    override fun onCreate() {
        super.onCreate()
        val api = NetworkModule.api
        sessionManager = SessionManager(this)
        authRepository = AuthRepository(api, sessionManager)
        studentRepository = StudentRepository(api, sessionManager)
        menuRepository = MenuRepository(api, sessionManager)
        orderRepository = OrderRepository(api, sessionManager)
    }
}
