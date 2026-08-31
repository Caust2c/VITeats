package com.viteats.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.viteats.app.data.SessionManager
import com.viteats.app.data.remote.Order
import com.viteats.app.data.remote.OrderListRequest
import com.viteats.app.data.remote.ProodleApi

class OrderRepository(
    private val api: ProodleApi,
    private val sessionManager: SessionManager
) {
    private val gson = Gson()

    suspend fun getRawOrders(): String {
        val userId = sessionManager.userIdentifier
        if (userId.isNullOrBlank()) {
            sessionManager.notifySessionExpired()
            return "Error: No User Identifier"
        }
        val response = api.getOrderList(OrderListRequest(userId))
        if (response.code() == 401 || response.code() == 403) {
            sessionManager.notifySessionExpired()
            return "Error: Session Expired"
        }
        if (!response.isSuccessful) return "Error: ${response.code()} ${response.message()}"
        return response.body() ?: "Empty body"
    }

    fun parseOrders(body: String): List<Order> {
        if (body.trim() == "0") return emptyList()
        if (body.startsWith("Error:")) return emptyList()
        
        return try {
            val type = object : TypeToken<List<Order>>() {}.type
            gson.fromJson(body, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOrders(): List<Order> {
        return parseOrders(getRawOrders())
    }

    suspend fun getOrderQR(orderId: String): String? {
        val response = api.getOrderQR(orderId)
        if (!response.isSuccessful) return null
        val body = response.body() ?: return null
        
        if (body.trim() == "0") return null
        return body
    }
}
