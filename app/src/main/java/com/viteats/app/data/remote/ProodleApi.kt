package com.viteats.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ProodleApi {
    @POST("GetOTPChecking")
    suspend fun verifyOtp(@Body request: LoginRequest): Response<List<LoginResponse>>

    @GET("getstudWBalinfo")
    suspend fun getBalanceInfo(@Query("rno") registrationNumber: String): Response<List<BalanceResponse>>

    @POST("getmenugroup")
    suspend fun getMenuGroups(): Response<String>

    @POST("Getcatlistnew")
    suspend fun getCategories(@Body body: Map<String, String>): Response<String>

    @POST("GetOptionMenuItems")
    suspend fun getMenuItems(@Body body: Map<String, String>): Response<String>

    @POST("GetOrderList")
    suspend fun getOrderList(@Body request: OrderListRequest): Response<String>

    @GET("orderQR")
    suspend fun getOrderQR(@Query("ordno") orderId: String): Response<String>
}
