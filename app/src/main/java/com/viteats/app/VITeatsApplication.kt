package com.viteats.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.NetworkModule
import com.viteats.app.data.repository.AuthRepository
import com.viteats.app.data.repository.MenuRepository
import com.viteats.app.data.repository.OrderRepository
import com.viteats.app.data.repository.StudentRepository

class VITeatsApplication : Application(), ImageLoaderFactory {
    lateinit var sessionManager: SessionManager
    lateinit var authRepository: AuthRepository
    lateinit var studentRepository: StudentRepository
    lateinit var menuRepository: MenuRepository
    lateinit var orderRepository: OrderRepository
    lateinit var cartRepository: com.viteats.app.data.repository.CartRepository

    override fun onCreate() {
        super.onCreate()
        val api = NetworkModule.api
        sessionManager = SessionManager(this)
        authRepository = AuthRepository(api, sessionManager)
        studentRepository = StudentRepository(api, sessionManager)
        menuRepository = MenuRepository(api, sessionManager)
        orderRepository = OrderRepository(api, sessionManager)
        cartRepository = com.viteats.app.data.repository.CartRepository()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(NetworkModule.okHttpClient)
            .crossfade(true)
            .build()
    }
}
