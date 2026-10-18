package com.viteats.app.data.remote

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://vit-proodle.expertsoftsys.com/api/"

    private class SimpleCookieJar : CookieJar {
        private val cookieStore = mutableMapOf<String, Cookie>()

        @Synchronized
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            for (cookie in cookies) {
                val key = "${cookie.domain}|${cookie.path}|${cookie.name}"
                cookieStore[key] = cookie
            }
        }

        @Synchronized
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val now = System.currentTimeMillis()
            cookieStore.entries.removeAll { it.value.expiresAt <= now }
            return cookieStore.values.filter { it.matches(url) }
        }
    }

    val okHttpClient = OkHttpClient.Builder()
        .cookieJar(SimpleCookieJar())
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Referer", "https://vit-proodle.expertsoftsys.com/Default")
                .addHeader("Origin", "https://vit-proodle.expertsoftsys.com")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
            level = HttpLoggingInterceptor.Level.NONE
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ProodleApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProodleApi::class.java)
    }
}
