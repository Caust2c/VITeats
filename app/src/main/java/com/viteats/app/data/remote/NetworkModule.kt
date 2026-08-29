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

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            for (cookie in cookies) {
                val key = "${cookie.domain}|${cookie.path}|${cookie.name}"
                cookieStore[key] = cookie
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore.values.filter { it.matches(url) }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(SimpleCookieJar())
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Referer", "https://vit-proodle.expertsoftsys.com/Default")
                .addHeader("Origin", "https://vit-proodle.expertsoftsys.com")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS // Log headers for cookie debugging, but not body for security
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